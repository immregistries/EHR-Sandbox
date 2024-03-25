# EhrSandboxUi

This angular application is the frontend of the EHR sandbox, alongside the EHR-Api backend. 

To change the base URI of the API :
 - when using `ng serve` to run the server, edit [src/environments/environment.ts](src/environments/environment.ts),
- for production mode edit [src/environments/environment.ts](src/environments/environment.prod.ts)

### WAR File
To obtain the war file for this application, you can execute script `bash compile_war.sh`, which will generate [dist/ehr.war](dist/ehr.war)

Otherwise executing `mvn clean package` in parent folder will generate a war file with both the ui and api


This project was generated with [Angular CLI](https://github.com/angular/angular-cli) version 13.2.5.

## Development server

Run `ng serve` for a dev server. Navigate to `http://localhost:4200/`. The app will automatically reload if you change any of the source files.

## Code scaffolding

Run `ng generate component component-name` to generate a new component. You can also use `ng generate directive|pipe|service|class|guard|interface|enum|module`.

## Build

Run `ng build` to build the project. The build artifacts will be stored in the `dist/` directory.

## Running unit tests

Run `ng test` to execute the unit tests via [Karma](https://karma-runner.github.io).

## Running end-to-end tests

Run `ng e2e` to execute the end-to-end tests via a platform of your choice. To use this command, you need to first add a package that implements end-to-end testing capabilities.

## Further help

To get more help on the Angular CLI use `ng help` or go check out the [Angular CLI Overview and Command Reference](https://angular.io/cli) page.