# Container for publishing Disk Space Usage to Prometheus

We needed a way to monitor disk space usage using prometheus in a kubernetes cluster. This container is designed as side-car container to achieve this. The metrics can be either scraped via http or sent to a pushgateway. The filesystems are to be mounted under `/data` or `/hostname`. The metrics get a label `name` with the directory name of the mount. If mounted under `/hostname`, the hostname will be prefixed, followed by a dash (`-`).

Environment:
* SERVER_PORT: if present, the metrics are served on this port
* PUSH_GATEWAY: if SERVER_PORT is not specified, the metrics are sent to the pushgateway on this host:port
* JOB_NAME: job name to use when publishing to a push gateway