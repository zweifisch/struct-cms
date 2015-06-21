general purpose structured content management system

## roles

- admin
    - manage editors
    - manage tempaltes
    - manage namespaces
- editor
    - create articles with a template under a namespace

## storage

use redis

### templates

- templates list of template names
- templates:#{name} json-serialized

### articles

- namespaces list of namespaces
- articles:#{namespace} list of articles ids under a namespace
- articles:#{namespace}:#{id} json-serialized
  - template

### users

- users list of user ids
- users:#{id} json-serialized
  - namespaces 
  - password

## build in types


