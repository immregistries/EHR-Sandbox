## EHR Sandbox deployment kit

Kit to easily deploy the EHR sandbox from the images hosted on dockerhub.

``.env`` file should be copied from ``.env.example`` and modified to change default passwords and secrets

To quickly deploy with default configuration

```
cp .env.example .env;
docker compose up;
```

or use bash script

```
bash deploy.sh
```

If ``.env`` already defined, run this command to deploy in docker a container

```
docker compose up;
```

default url is localhost:9091/ehr/#/home, port can be changed in docker-compose.yml

### Environments variables used :

- see ``.env.example``
