# JsonQL

## Implementations

> Currently only support [hibernate5](hibernate5).

* [Hibernate5](hibernate5): Currently only supported;
* [Hibernate6](hibernate6): Planned;

## DML

### Common fields

```json lines
{
  "table": "table to operate",
  "operation": "select/update/insert/delete/logic_delete"
}
```

#### context

Parameters from the context can be referenced within the following content using a specific format for text fields, as follows: `_{xxxx}_`.

##### insert

```json lines
{
  "table": "someTable",
  "operation": "insert",
  "data": {
    "title": "_{titleArg}_"
  }
}
```

##### update

```json lines
{
  "table": "someTable",
  "operation": "update",
  "effectMaxRows": 1,
  "data": {
    "title": "_{titleArg}_"
  },
  "condition": {
    "id": 1
  }
}
```

##### Update/Delete limitation

```json lines
{
  "table": "someTable",
  "operation": "update",
  "effectMaxRows": 1,
  "condition": {
    "id": 1
  }
}
```

**effectMaxRows**
> The maximum number of rows affected during an update or delete operation. The default value is 1. If the number of affected rows exceeds this value, the transaction is rolled back.;

* -1: Indicates no limit;
* =0: Implies that the transaction will be rolled back regardless of the outcome;
* \>0: Specifies that the transaction should be rolled back if the number of affected rows exceeds this value;

##### Condition on update/delete/logic_delete/select

``` json lines
{
  "table": "someTable",
  "operation": "select",
  "page": 1,
  "pageSize": 10,
  "count": false,
  "distinct": false,
  "condition": {
    "updated_at": {
      "$lt": "_{serverTime}_"
    }
  }
}
```

### Conditions

| Operator    | description              |
|-------------|--------------------------|
| =           | equal                    |
| \>          | greater than             |
| <           | less than                |
| \>=         | greater than or equal to |
| <=          | less than or equal to    |
| contains    | %like%                   |
| starts      | like%                    |
| ends        | %like                    |
| null        | is null                  |
| and         | and group                |
| or          | or group                 |
| !<Operator> | negative                 |

**Equal**

```json lines
// where id = 1
{
  "condition": {
    "id": 1
  }
}
```

**Others**

``` json lines
{
  "condition": {
    "id": {
      "Operator": 1
    }
  }
}
```

**in**

``` json lines
// where id in ('1','2','3')
{
  "condition": {
    "id": {
      "in": ["1", "2", "3"]
    }
  }
}
```

**null/!null**

``` json lines
// where id is/not null
{
  "condition": {
    "id": {
      "null": null
      "!null": null
    }
  }
}
```

## Order by

``` json lines
// order by id desc, age asc
{
  "orderBy": [
    "-id",
    "+age"
  ]
}
```

## Group by

``` json lines
// group by gender
{
  "groupBy": [
    "gender"
  ]
}
```

## Join
```json lines
// from xxx root inner join full_name fn on root.name_id = fn.id inner join family f on fn.last_name = f.title
{
  "table": "xxx",
  "alias": "root",
  "join": [
    {
      "referencedColumn": "name_id",
      "table": "full_name",
      "joinColumn": "id",
      "alias": "fn"
    },
    {
      "handleTable": "full_name",
      "referencedColumn": "last_name",
      "table": "family",
      "joinColumn": "title",
      "alias": "f"
    }
  ]
}
```