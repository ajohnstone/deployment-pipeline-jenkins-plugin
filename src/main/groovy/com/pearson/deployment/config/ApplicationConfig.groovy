package com.pearson.deployment.config

// import hudson.model.*

class ApplicationConfig extends ConfigReader implements Serializable {
  ApplicationConfig(String filename) {
    readConfig(filename)

    attributes.applications?.each {
      validate_application(it)
    }
  }

  // ApplicationConfig(AbstractBuild<?, ?> build, String filename) {
  //   readConfig(build, filename)
  //
  //   attributes.applications?.each {
  //     validate_application(it)
  //   }
  // }

  private def validate_application(def app) {
    validate {
      required = [
        "name",
        "runtime",
        "version"
      ]
      attributes = app
    }
  }
}
