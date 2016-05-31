#!/usr/bin/groovy
def call(body) {
    // evaluate the body block, and collect configuration into the object
    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()

    def rc = """
{
        "kind": "Service",
        "apiVersion": "v1",
        "metadata": {
            "name": "${env.JOB_NAME}",
            "creationTimestamp": null,
            "labels": {
                "component": "${env.JOB_NAME}",
                "container": "${config.label}",
                "project": "${env.JOB_NAME}"
            }
        },
        "spec": {
            "ports": [
                {
                    "protocol": "TCP",
                    "port": 80,
                    "targetPort": ${config.port}
                }
            ],
            "selector": {
                "component": "${env.JOB_NAME}",
                "container": "${config.label}",
                "project": "${env.JOB_NAME}"
            },
            "type": "ClusterIP",
            "sessionAffinity": "None"
        }
}

{
    "kind": "Deployment",
    "apiVersion": "extensions/v1beta1",
    "metadata": {
        "name": "${env.JOB_NAME}",
        "labels": {
            "component": "${env.JOB_NAME}",
            "container": "${config.label}",
            "project": "${env.JOB_NAME}"
        }
    },
    "spec": {
        "replicas": ${config.replicas},
        "template": {
            "metadata": {
                "labels": {
                    "component": "${env.JOB_NAME}",
                    "container": "${config.label}",
                    "project": "${env.JOB_NAME}"
                }
            },
            "spec": {
                "containers": [
                    {
                        "name": "${env.JOB_NAME}",
                        "image": "${config.image}:${config.version}",
                        "ports": [
                            {
                                "name": "web",
                                "containerPort": ${config.port},
                                "protocol": "TCP"
                            }
                        ],                            
                        "imagePullPolicy": "Always"
                    }
                ],
                "terminationGracePeriodSeconds": 15,
                "dnsPolicy": "ClusterFirst"
            }
    }}}
    """

    echo 'using Kubernetes resources:\n' + rc
    return rc

  }
