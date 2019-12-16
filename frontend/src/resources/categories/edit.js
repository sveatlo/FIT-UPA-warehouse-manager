import React from "react";
import {
  Create,
  Edit,
  // DisabledInput,
  ReferenceInput,
  SelectInput,
  SimpleForm,
  TextInput
} from "react-admin";

const CustomizationForm = props => (
  <SimpleForm {...props}>
    {/*props.record.id ? <DisabledInput source="id" /> : <span></span>*/}
    <TextInput source="name" />
    <ReferenceInput source="parent" reference="categories">
      <SelectInput optionText="name" />
    </ReferenceInput>
  </SimpleForm>
);

export const CategoriesCreate = props => (
  <Create {...props}>
    <CustomizationForm {...props} />
  </Create>
);

export const CategoriesEdit = props => (
  <Edit {...props}>
    <CustomizationForm {...props} />
  </Edit>
);
