import React from "react";
import {
  Datagrid,
  List,
  ReferenceField,
  Responsive,
  SimpleList,
  TextField
} from "react-admin";

export const CategoriesList = props => (
  <List {...props}>
    <Responsive
      small={<SimpleList primaryText={record => record.name} />}
      medium={
        <Datagrid rowClick="edit">
          <TextField source="id" />
          <TextField source="name" sortable={false} />
          <ReferenceField
            source="parent"
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
