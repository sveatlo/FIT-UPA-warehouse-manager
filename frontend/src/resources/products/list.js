import React from "react";
import {
  Datagrid,
  List,
  ReferenceField,
  DateField,
  Responsive,
  SimpleList,
  TextField
} from "react-admin";

export const ProductsList = props => (
  <List {...props}>
    <Responsive
      small={<SimpleList primaryText={record => record.name} />}
      medium={
        <Datagrid rowClick="edit">
          <TextField source="id" />
          <TextField source="name" sortable={false} />
          <TextField source="price" sortable={false} />
          <ReferenceField
            source="category_id"
            reference="categories"
            allowEmpty={true}
            sortable={false}
          >
            <TextField source="name" />
          </ReferenceField>
        </Datagrid>
      }
    />
  </List>
);
