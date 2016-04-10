# MERN-Cljs

## What is it?

- A web stack with
  [MongoDB](https://docs.mongodb.org/manual/introduction/)
  [Express](http://expressjs.com)
  [React](https://facebook.github.io/react/)
  [Node.js](https://nodejs.org)
  and [ClojureScript](https://github.com/clojure/clojurescript)
  ([DynamoDB](https://aws.amazon.com/dynamodb/) is also supported)
- Build React backed web apps in Clojure (
  [reagent](https://github.com/reagent-project/reagent) + [kioo](https://github.com/ckirkendall/kioo))
- Login with a social network account via
  [Passport.js](http://passportjs.org)
- Async worker via [RabbitMQ](https://www.rabbitmq.com)

For now, this repository serves to share the knowledge on how to achieve above
and I don't intend to release a library or framework :)

## Motivation

I wanted to build web application very quickly. I find predominantly functional
language such as Clojure to be a very powerful choice for speeding up the
application development. Meanwhile, I wanted to leverage the development
by the tools other people built and tested well already. So I chose to combine
ClojureScript and node.js. This way, I can take advantage of very rich set of
JavaScript libraries managed by [npm](https://www.npmjs.com). I don't have to
switch language (e.g. Python <-> JavaScript) to develop backend and frontend.

## What you need to install

1. Node.js
2. MongoDB (or DynamoDB)
3. Leiningen

## How to run locally

Run MongoDB

```
brew services start mongodb
```

Run the example API server

```
cd example/api 
lein compile
lein run
```

Run the web app 

```
cd example/www
lein compile
lein run
```

Point the browser to `http://localhost:1337/profile`
(API server is served on port 5000 by default.)

## About the source code

This section shows where you should look at when you want to extend/develop
your application.

### example/common

It contains configurations and object models:

#### Configuration file

example/common/src/common/config.cljs 

#### Object models

This web stack uses [Mongoose](http://mongoosejs.com) for object modeling
([Dynamoose](https://github.com/automategreen/dynamoose) when used with
DyanmoDB). See their docs for the Schema and Model generation.

In the example app, you can find the user schema at
example/common/src/common/models.
Also check out example/common/src/common/model.cljs.

### example/api

API app.

- example/api/src/api/core.cljs: Logic to start Express.js.
- example/api/src/api/handlers.cljs: Handlers for each API endpoint.

### example/www

WWW app. It contains backend and frontend code.

#### Backend

- example/www/src/backend/server/core.cljs: Logic to start Express.js.
- example/www/src/backend/server/handlers.cljs: Handlers for each web page. Triggers
  server side rendering.
- example/www/src/backend/server/views.cljs: Static view template with kioo.

#### Frontend

- example/www/src/frontend/app/core.cljs: Frontend logic. Trace from Ln 73
  (def route) to find how profile is pulled from API server.
- example/www/src/frontend/app/atom.cljs: Application state.
- example/www/src/frontend/app/views.cljs: Dynamic view implementation with kioo.

#### HTML templates

- example/resources/public

### example/worker

Worker instance that executes async tasks. This is turned off by default since
it requires RabbitMQ. See Async tasks section to find how to turn it on.

- example/worker/src/worker/core.cljs: Core async task logic and example task
  ("log" function)

## About API Authentication process

Frontend app tries to fetch `me` resource. If it fails, it will redirect to
Social auth. WWW backend authenticates via Passport.js and write information
on MongoDB with a one time API token. Token is passed on to the frontend and
it is used to API auth. In the example app, it will be redirected to profile
page to fetch `me` resource with proper authentication.

## Async tasks

If you run async task, install and run RabbitMQ first:

```
brew install rabbitmq
brew services start rabbitmq
```

Then uncomment `create-amqp-conn` around Ln. 37 of `example/api/src/api/core.cljs`:

```
...
(defn server [success]
; Activate the next line if you want to run async task 
; (create-amqp-conn amqp-endpoint)
...
```

Recompile and run api instance:

```
cd example/api
lein compile
lein run
```

Start worker instance:

```
cd example/worker
lein compile
lein run
```

Activate RabbitMQ management dashboard plugin:

```
rabbitmq-plugins enable rabbitmq_management
```

Point your browser to http://localhost:15672/#/connections to see the connections.
There should be one from api instance and one from worker.

Point your browser to http://localhost:5000/task to trigger a trivial task.
Check the console of worker instance to see if the task was received. This should
look like:

```
 [*] Waiting for messages. To exit press CTRL+C
 [x] Received message hello
```

When http://localhost:5000/task is requested (GET), the api server code
(handle_task function in example/api/src/api/handlers.cljs) send the message
to RabbitMQ. The worker app takes the message to execute the log function
in example/worker/src/worker/core.cljs.

The log function is not a good example of async task. The idea is to avoid
API server to process time consuming tasks. In production, we can deploy multiple
worker instances to do pararell task execution.

## DynamoDB

You can use DynamoDB instead of MongoDB.

To test locally, install and start
[DynamoDB local](http://docs.aws.amazon.com/amazondynamodb/latest/developerguide/Tools.DynamoDBLocal.html).

Then edit example/common/src/common/config.cljs:

```
; mongodb or dynamodb
(def DATABASE "mongodb")
(def DB-ENDPOINT (str "mongodb://" MONGODB-DOMAIN ":" MONGODB-PORT "/" MONGODB-DBNAME))

; (def DATABASE "dynamodb")
; Keep DynamoDB endpoint nil to use AWS (non-local)
; (def DB-ENDPOINT (str "http://" DYNAMODB-DOMAIN ":" DYNAMODB-PORT))
```

Recompile and start www and api (and worker) apps.

## Issues

- Google Auth requires consent every time the page reloads

## TODOs

- Production deployment code.
- Add Twitter auth

## References

- [Easy Node Authentication: Setup and Local | Scotch](https://scotch.io/tutorials/easy-node-authentication-setup-and-local)
