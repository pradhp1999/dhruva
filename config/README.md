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
  
### Current Network configuration

We bind to 5070 as the internal port and 11500/11501 as the external port. This is just a convention and can be changed.
These 3 port mappings should align with the external and internal mappings in our helm charts. 
 
The following networks are used for now:
- One TLS Pub network with hostPort configuration, so that dhruva.{{dc}}.int.meetapi.webex.com is inserted in the headers.
- One UDP Pub network, same as above.
- One TLS Prv network, without hostPort

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
## Rendering configs

`meet-apps-charts` has the helm cfg and final helm charts for dhruva. So the following commands 
need to be run in that repo:

**Note:** Make sure you run this with helm v2.

### Update dependencies and Generate configs
``` sh
cd stable/dhruva
helm dependency update; helm cfg build --define service=dhruva,env=wsjcint01,cloud=int,\
profile=dhruva,cceEnv=meetpaas-int
```

### Lint chart

``` sh
cd dhruva
helm lint . --namespace dhruva -f ../meet-app-base/base_values.yaml,values.yaml,data/helm-values.yaml \
--set vault.token="sdsd",vault.address="Asdsd",clusterName=wsjcint01
```
### Render templates locally

``` sh
helm template -n dhruva --namespace dhruva  -f ../meet-app-base/base_values.yaml,values.yaml,data/helm-values.yaml \
--set image.credentials.username=asdasd,image.credentials.password=defdef,\
vault.token=qweqwe,vault.address=123123,clusterName=wsjcint01,image.tag=6493-190514-283b6 .
```

Please see the 
[meet-apps-charts README](https://sqbu-github.cisco.com/WebexPlatform/meet-apps-charts) for 
further details.