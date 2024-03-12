## EHR Sandbox deployment kit 
Step 1 Install Docker Image
```
 docker load -i ehr-sandbox-image.tar;
```

Step 2 Run this command to deploy in docker container
```
 docker compose up;
```

default url is localhost:9091/ehr/#/home, port can be changed in docker-compose.yml