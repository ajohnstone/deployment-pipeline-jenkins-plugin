# Kubernetes Continuous Deployment Pipeline

An open source Jenkins pipeline tool purpose built for Kubernetes containers.

* [Quickstart Sample App](quickstart.md)
* [Project Details](#details)


## Requirements:
Our Jenkins plugin deploys all containers to Kubernetes minions that have a label of

```
role=minion
```
This can be added to your workers/minions with
```
kubectl label nodes <node_name> role=minion
```

<a id="details"></a>
## Project Details

Jenkins uses a custom workflow to build and deploy applications. The whole CI/CD pipeline is built using just three manifest files, which have very distinctive roles in the build process.

-   **build.bitesize** - defines how to build one or more components
-   **application.bitesize** - defines how to build one or more
    applications using required components and
    external dependencies
-   **environments.bitesize** - defines how to layout environments,
    which applications (services) to run in them, what tests to run
    against your applications and the method to deploy applications.
    Builds out the whole CI/CD pipeline


Your project will store these files in a git repository. It can be either your source repository, or a repository dedicated just to managing these three files. At Pearson we've found our dev teams prefer to manage these files separately from their code repositories.<br><br>

**Global Definition** - <br>
`project` - every .bitesize config file must specify the project name. This allows Jenkins to tie the various config files together as one complete Jenkins workflow.<br>

Ex. `project: docs`

## environments.bitesize

Consists of:<br>
  * [project name](#projectname)<br>
  * [environments](#environments)<br>
    * [name](#environmentname)
    * [deployment method](#deploymentmethod)<br>
    * [services](#services)<br>
    * [tests](#tests)<br>
    * [health checks](#healthchecks)<br>


environments.bitesize contains building blocks for each environment you intend to deploy/manage. In order for Jenkins to manage multiple environments from a single Jenkins container, a standard naming convention for Kubernetes namespaces are required.<br><br>
<a id="projectname"></a>
naming convention:<br>
`<project_name>-<three_letter_env_name>`<br><br>
Ex. example-dev<br>
Ex. example-tst<br>
Ex. example-prd<br>
<br>

Here is an example of a complete environments.bitesize config.<br>

```
project: docs-dev
environments:
  - name: production
    namespace: docs-dev
    deployment:
      method: rolling-upgrade
    services:
      - name: docs-app
        external_url: kubecon.dev-bite.io
        port: 80 # this is the port number the application responds on in each container/instance/pod
        ssl: "true"
        replicas: 2
    tests:
      - name: docs site tests
        repository: git@github.com:pearsontechnology/kubecon_docs_tests.git
        branch: master
        commands:
          - shell: echo serviceBaseUrl=http://escrow.reg.svc.cluster.local:3004 > integration/test.properties
    health_check:
      command:
        - /bin/health_script.sh # Path to your script. Exit code 0 means success
      initial_delay: 30 # Time in seconds to wait for a fresh instance
      timeout: 60 # Time in seconds to wait before health check script times out
```
<br>
Breaking down the environments.bitesize config.<br>

<a id="environments"></a>
<a id="environmentname"></a>
### `name`
Every environment starts with a `name`.<br>
Within each environment we must specify the namespace in which the environment deploys to. <br>
```
- name: production
  namespace: docs-dev
```
<br>
<a id="deploymentmethod"></a>
### `deployment method`
<br>
Currently the only available deployment method is `rolling-update`.
A `mode` (optional) can also be specified with the deployment method. This is generally used if a manual deployment is desired.
```
deployment:
  method: rolling-upgrade
  mode: manual
```
<br>
<a id="services"></a>
### `services`
in the pipeline make up multiple Kubernetes Resources.
  * Kubernetes Service
  * Ingress (Optional)
  * Number of replicas in the replica set.<br>

The name for each service must match the application name for the given app in applications.bitesize.<br>
Notice we also need to specify what port(s) the containers will use for each service.<br>
Not specifying an `external_url` will result in a Kubernetes Ingress not being created.<br>
Environment vars can be specified here with `env`.<br>

**Note** : `env` is not recommended because it requires a redeploy of the app to modify the environment variable specified.

```
services:
  - name: docs-app
    external_url: kubecon.dev-bite.io
    port: 80 # this is the port number the application responds on in each container/instance/pod
    replicas: 2
    env:
    - name: MONGO_DB_1
      value: escrow
```

<a id="tests"></a>
### `tests`
Tests assume a repository will be required to pull in the tests to run.<br>
These tests can be anything so long as the correct dependencies for Jenkins to execute the tests have been installed.<br>
There is no limit on the number of tests that can be created so long as they have a unique name.<br>

```
tests:
  - name: docs site tests
    repository: git@github.com:pearsontechnology/kubecon_docs_tests.git
    branch: master
    commands:
      - shell: echo serviceBaseUrl=http://docs.default.svc.cluster.local:80 > integration/test.properties
```
<br>

<a id="healthcheck"></a>
### `health_check`
are directly associated to liveness probes in Kubernetes.<br>
Notice how `initial_delay` and `timeout` set by seconds.<br>

```
health_check:
  command:
    - /bin/health_script.sh # Path to your script. Exit code 0 means success
  initial_delay: 30 # Time in seconds to wait for a fresh instance
  timeout: 60 # Time in seconds to wait before health check script times out
```

## application.bitesize



## build.bitesize






<br>
First, let’s define some terms to understand what exactly they mean in our context:

| Term | Definition |
| --- | :--- |
| *Component* | Individual code repository necessary to create an Application. Your application will have one or more internal components. |
| *Application*      | Artifacts as a single Artifact necessary to create a running instance (a collection of components). Application includes the full stack required to run an instance. |
| *Build* | Process that outputs artifacts (Debian Package, Docker Image, etc…) |
| *Job* | An instance (success or fail) of a Build |
| *Build Definition* | What actions need to be executed to generate the output artifact. |
| *Job Definition* | Specifics related to the Job, generally Version Number, Tags, .... |
| *Build Dependency* | A tool or utility necessary to create a Component Artifact. |
| *Service* | Instance (or multiple grouped instances in HA mode) of a running application. |
| *Environment* | Collection of services, grouped together to represent a fully working application stack. |
