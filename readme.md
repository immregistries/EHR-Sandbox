## EHR Sandbox deployment kit 
Step 1 Install Docker Image
```
 docker load -i ehr-image.tar;
```

Step 2 Run this command to deploy in docker container
```
 docker compose up;
```

default url is localhost:9091/ehr, port can be changed in docker-compose.yml

One issue was noticed that first time launching a container will often fail, with the next start being successful, this is being investigated.