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
  DateTimeInput,
  ImageField,
  ImageInput,
  REDUX_FORM_NAME,
  required
} from "react-admin";
import Grid from "@material-ui/core/Grid";
import Button from "@material-ui/core/Button";
import CircularProgress from "@material-ui/core/CircularProgress";
import { fetchUtils } from "ra-core";
import config from "../../config";
import { Stage, Layer, Rect, Circle } from "react-konva";
import Konva from "konva";

const ProductGeometryEditor = ({ productGeometry, ...props }) => {
  return (
    <>
      <div
        style={{
          border: "1px solid black",
          width: "400px",
          height: "200px"
        }}
      >
        <FormDataConsumer>
          {({ formData, ...moreProps }) => {
            if (formData && !formData.geometry) {
              formData.geometry = {
                x: 0,
                y: 0,
                radius: 10,
                width: 10,
                height: 10
              };
            }
            const onUnitDragEng = ({ target }) => {
              if (!formData.geometry) {
                formData.geometry = {
                  x: 0,
                  y: 0
                };
              }
              formData.geometry.x = target.attrs.x;
              formData.geometry.y = target.attrs.y;
            };
            if (productGeometry && productGeometry.type === "circle") {
              return (
                <>
                  <Stage width={400} height={200}>
                    <Layer>
                      <Circle
                        draggable
                        radius={productGeometry.radius * 4}
                        x={productGeometry.radius * 4}
                        y={productGeometry.radius * 4}
                        fill="red"
                        onDragEnd={onUnitDragEng}
                      />
                    </Layer>
                  </Stage>
                </>
              );
            } else if (
              productGeometry &&
              productGeometry.type === "rectangle"
            ) {
              return (
                <>
                  <Stage width={400} height={200}>
                    <Layer>
                      <Rect
                        draggable
                        x={0}
                        y={0}
                        width={productGeometry.width * 4}
                        height={productGeometry.height * 4}
                        fill="green"
                        onDragEnd={onUnitDragEng}
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
const ProductUnitForm = props => {
  const [productGeometry, setProductGeometry] = useState({});

  return (
    <SimpleForm {...props}>
      <ReferenceInput
        source="product_id"
        reference="products"
        validate={required()}
        onChange={({ target }) => {
          fetchUtils
            .fetchJson(`${config.apiUrl}/products/${target.value}/`, {
              method: "GET"
            })
            .then(data => {
              setProductGeometry(data.json.data.geometry);
            })
            .catch(() => {
              throw new Error("Error");
            });
        }}
      >
        <SelectInput optionText="name" />
      </ReferenceInput>
      <DateTimeInput source="check_in" />
      <DateTimeInput source="check_out" />
      <FormDataConsumer>
        {({ formData, ...moreProps }) => {
          return <ProductGeometryEditor productGeometry={productGeometry} />;
        }}
      </FormDataConsumer>
    </SimpleForm>
  );
};

export const ProductUnitsCreate = props => (
  <Create {...props}>
    <ProductUnitForm create {...props} />
  </Create>
);

export const ProductUnitsEdit = props => {
  return (
    <Edit {...props}>
      <ProductUnitForm edit {...props} />
    </Edit>
  );
};
