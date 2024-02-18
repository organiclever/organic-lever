# Market Sentinel

Hi there! Welcome to Market sentinel app.

## Up and Running Locally

1. Make sure you read the root's [README](../../README.md).
2. This app will need `app_secrets.edn` to present in the project's root folder. This file is not committed. You can see its file structure in the [app_secrets_example.edn file](./app_secrets_example.edn).
3. After that, execute the preparation script for this project `clj -X:prepare`. This prepare script will do 2 things:
   1. Create `test/tmp/` directory which will be used for testing.
   2. Setup Kaocha for testing.
4. Run `docker-compose.yaml` file using `docker compose up`. This will setup the database for local development.
5. For local development, there are several things to note:
   1. This project will use `dbml` file to create a migration needed for local development. You can run `npm run db:gen-sql:market-sentinel` specified in the root's `package.json` file.
   2. This project will need a PostGresQL database for development. Having run the previous step, you just need to run `clj -X:db-init` (init the database schema) and `clj -X:db-seed` (seed the database) to prepare the database for development.
   3. If you want to delete the db schema, you can use `clj -X :db-delete` command, or if you want to also init the schema after deletion, you can use `clj -X :db-reset`.
6. There are 2 types of test in this project. Please note that this is because we want to make sure that it can be run in the right place and infrastructure in the future (i.e., integration test will most-likely slower to run than unit-test).
   1. Unit test
      1. This test not allowed to do "inter process" (e.g. run in browser, communicate with DB).
      2. You can run it with `./bin/kaocha unit` for single run, or `./bin/kaocha unit --watch` for watch mode.
      3. Unit tests don't have `^:integration` notation.
   2. Integration test
      1. This test allowed to do "inter process" but not networking.
      2. You can run it with `./bin/kaocha integration` for single run, or `./bin/kaocha integration --watch` for watch mode.
      3. Integration tests have `^:integration` notation.
