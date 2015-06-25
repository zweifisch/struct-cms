structured-content management system

## roles

- admin
  - manage editors
  - manage tempaltes
  - manage namespaces
  - manage articles
- editor
  - manage articles under given namespaces

## redis schema

### templates

- `templates` list of template ids
- `templates:#{name}` template detail

### namespaces

- `namespaces` list of namespaces ids
- `namespaces:#{id}` namespace detail
  - `tempalte`
- `namespaces:#{id}:articles` list of articles ids under a namespace

### articles

- `articles:#{namespace}:#{id}` list of article ids under a namespace

### users

- `users` list of user ids
- `users:#{id}` user detail
  - `password`
- `users:#{id}:namespaces` list of namespace ids user can access

## templates

```yaml
- name: url
  label: URL
  type: string
- name: title
  type: string
  label: Title
```
