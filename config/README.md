# Dhruva Configuration

## Overview

The files in this folder serve the following purposes:

- *dhruva.yaml.tpl* - Main template file that has all the keys and template variables for where to pull the values from.
  - e.g. `APPLICATION_NAME: '{{ .Dhruva.deploy.APPLICATION_NAME }}'`
- *dhruva_default_values.yaml.tpl* - If your config has a default values that needs to be picked up in case it is not 
  mentioned in an environment-specfic file, put those values here.
  - e.g. `MetricsPort='8125'`
- *meetpaas-int__dhruva__.yaml.tpl* - These are variables that are specific to the `meetpaas-int` environment. 
  - e.g. `metricsPublicUrl`, which is a per-environment URL.
  
## Other Repos

- [int-app-clusters](https://sqbu-github.cisco.com/WebexPlatform/int-app-clusters) - This repo has variables for 
  `meetpaas-int` that dhruva can use.
  - e.g. `MeetPaas.kafka.hostPortAddress`
- [meet-app-charts](https://sqbu-github.cisco.com/WebexPlatform/meet-apps-charts) - This repo has the helm chart. 
  A `requirements.yaml` file in `dhruva`'s folder tells it to get the configs from `dhruva/config` and merge it with 
  variables present in `int-app-clusters`.

## Verification

Run a command like this to manually verify the content:

```shell script
yamllint -d \
  '{extends: default, rules: {line-length: {max: 2048}, indentation: {spaces: 2, indent-sequences: consistent}, document-start: disable}}' \
  meetpaas-int__dhruva__.yaml.tpl
```
## Roadmap

- Some kind of linting/verification is required. Errors need to be caught before merge.
