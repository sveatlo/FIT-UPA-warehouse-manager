import React, { Fragment, useState } from "react";
import { change } from "redux-form";
import {
  Create,
  Edit,
  // DisabledInput,
  ReferenceInput,
  FormDataConsumer,
  NumberInput,
  SelectInput,
  SimpleForm,
  TextInput,
  ImageField,
  ImageInput,
  REDUX_FORM_NAME,
  required
} from "react-admin";
import { Field } from "react-final-form";
import Grid from "@material-ui/core/Grid";
import Button from "@material-ui/core/Button";
import CircularProgress from "@material-ui/core/CircularProgress";
import { fetchUtils } from "ra-core";
import config from "../../config";
import { Stage, Layer, Rect, Circle } from "react-konva";
import Konva from "konva";

const RotateButton = ({
  record,
  angle,
  setRotateButtonsDisabled,
  dispatch,
  ...props
}) => {
  // if (isLoading) {
  //   return <LinearProgress />;
  // }
  // if (isError) {
  //   return <Error />;
  // }

  return (
    <Button
      {...props}
      variant="contained"
      onClick={() => {
        setRotateButtonsDisabled(true);
        fetchUtils
          .fetchJson(
            `${config.apiUrl}/products/${record.id}/image/rotate/${angle}/`,
            {
              method: "POST"
            }
          )
          .then(() => {
            console.log("Ok");
            setRotateButtonsDisabled(false);
            dispatch(
              change(REDUX_FORM_NAME, "image_data", {
                uri: `${config.apiUrl}/products/${
                  record.id
                }/image?time=${new Date().toISOString()}`
              })
            );
          })
          .catch(() => {
            setRotateButtonsDisabled(true);
          });
      }}
    >
      Rotate {angle} degrees
    </Button>
  );
};

const ProductGeometryEditor = () => {
  return (
    <>
      <SelectInput
        source="geometry.type"
        choices={[
          { id: "circle", name: "Circle" },
          { id: "rectangle", name: "Rectangle" }
        ]}
      />
      <FormDataConsumer>
        {({ formData, ...rest }) => {
          if (formData && !formData.geometry) {
            formData.geometry = {
              x: 0,
              y: 0,
              radius: 10,
              width: 10,
              height: 10
            };
          }
          if (formData.geometry && formData.geometry.type === "circle") {
            return <NumberInput source="geometry.radius" {...rest} />;
          } else if (
            formData.geometry &&
            formData.geometry.type === "rectangle"
          ) {
            return (
              <>
                <NumberInput source="geometry.width" {...rest} />
                <NumberInput source="geometry.height" {...rest} />
              </>
            );
          }
        }}
      </FormDataConsumer>
      <div
        style={{
          border: "1px solid black",
          width: "400px",
          height: "200px"
        }}
      >
        <FormDataConsumer>
          {({ formData }) => {
            console.log("NOOOOOOOOOO", formData);
            if (formData.geometry && formData.geometry.type === "circle") {
              return (
                <>
                  <Stage width={400} height={200}>
                    <Layer>
                      <Circle
                        radius={formData.geometry.radius * 4}
                        x={formData.geometry.radius * 4}
                        y={formData.geometry.radius * 4}
                        fill="red"
                      />
                    </Layer>
                  </Stage>
                </>
              );
            } else if (
              formData.geometry &&
              formData.geometry.type === "rectangle"
            ) {
              return (
                <>
                  <Stage width={400} height={200}>
                    <Layer>
                      <Rect
                        x={0}
                        y={0}
                        width={formData.geometry.width * 4}
                        height={formData.geometry.height * 4}
                        fill="green"
                      />
                    </Layer>
                  </Stage>
                </>
              );
            }
          }}
        </FormDataConsumer>
      </div>
    </>
  );
};

const ProductForm = props => {
  const [rotateButtonsDisabled, setRotateButtonsDisabled] = useState(false);
  return (
    <SimpleForm {...props}>
      <TextInput source="name" validate={required()} />
      <ReferenceInput
        source="category_id"
        reference="categories"
        validate={required()}
      >
        <SelectInput optionText="name" />
      </ReferenceInput>
      <NumberInput source="price" validate={required()} />
      <ImageInput
        source="image_data"
        label="Product image"
        accept="image/*"
        options={{
          onDropAccepted: () => {
            setRotateButtonsDisabled(true);
          }
        }}
      >
        <ImageField source="uri" />
      </ImageInput>
      <FormDataConsumer>
        {({ formData, dispatch, ...moreProps }) => {
          return props.edit && formData && !!formData.image_data ? (
            <>
              <RotateButton
                disabled={rotateButtonsDisabled}
                setRotateButtonsDisabled={setRotateButtonsDisabled}
                record={formData}
                dispatch={dispatch}
                angle={90}
              />
              <RotateButton
                disabled={rotateButtonsDisabled}
                setRotateButtonsDisabled={setRotateButtonsDisabled}
                record={formData}
                dispatch={dispatch}
                angle={-90}
              />
            </>
          ) : (
            <></>
          );
        }}
      </FormDataConsumer>
      <ProductGeometryEditor />
    </SimpleForm>
  );
};

export const ProductsCreate = props => (
  <Create {...props}>
    <ProductForm create {...props} />
  </Create>
);

export const ProductsEdit = props => (
  <Edit {...props}>
    <ProductForm edit {...props} />
  </Edit>
);
