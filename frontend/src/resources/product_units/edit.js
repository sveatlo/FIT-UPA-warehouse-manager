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
            console.log("NOOOO geom", formData.geometry);
            if (formData && !formData.geometry) {
              formData.geometry = {
                x: 0,
                y: 0,
                radius: 10,
                width: 10,
                height: 10
              };
            }
            if (productGeometry && productGeometry.type) {
              formData.geometry.type = productGeometry.type;
              formData.geometry.radius = productGeometry.radius;
              formData.geometry.width = productGeometry.width;
              formData.geometry.height = productGeometry.height;
            }
            const onUnitDragEng = ({ target }) => {
              formData.geometry.x = target.attrs.x / 4;
              formData.geometry.y = target.attrs.y / 4;
            };
            if (formData.geometry.type === "circle") {
              console.log("NOOOOO geom circle");
              return (
                <>
                  <Stage width={400} height={200}>
                    <Layer>
                      <Circle
                        draggable
                        radius={formData.geometry.radius * 4}
                        x={formData.geometry.x * 4}
                        y={formData.geometry.y * 4}
                        fill="red"
                        onDragEnd={onUnitDragEng}
                      />
                    </Layer>
                  </Stage>
                </>
              );
            } else if (formData.geometry.type === "rectangle") {
              console.log("NOOOOO geom rectangle");
              return (
                <>
                  <Stage width={400} height={200}>
                    <Layer>
                      <Rect
                        draggable
                        x={formData.geometry.x * 4}
                        y={formData.geometry.y * 4}
                        width={formData.geometry.width * 4}
                        height={formData.geometry.height * 4}
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
  const [overlappingUnits, setOverlappingUnits] = useState(null);
  const [spatialRelations, setSpatialRelations] = useState(null);

  const getOverlapppingUnits = id => {
    fetchUtils
      .fetchJson(`${config.apiUrl}/product_units/${id}/overlapping/`, {
        method: "GET"
      })
      .then(res => {
        setOverlappingUnits(res.json.data);
      })
      .catch(() => {
        alert("error");
      });
  };
  const getSpatialRelations = id => {
    fetchUtils
      .fetchJson(`${config.apiUrl}/product_units/${id}/relations/`, {
        method: "GET"
      })
      .then(res => {
        setSpatialRelations(res.json.data);
      })
      .catch(() => {
        alert("error");
      });
  };
  if (overlappingUnits === null) {
    getOverlapppingUnits(props.id);
  }
  if (spatialRelations === null) {
    getSpatialRelations(props.id);
  }

  const OverlappingUnitsComponent = ({ ids }) => {
    if (ids === undefined || ids === null || ids.length === 0) {
      return <></>;
    }

    console.log("SUPER NOOOOOOOOOOOOOOOO", ids);

    return (
      <>
        <h2>Overlapping units:</h2>
        <ul>
          {ids.map(item => {
            return <li>{item}</li>;
          })}
        </ul>
      </>
    );
  };

  const SpatialRelationsComponent = ({ data }) => {
    if (!data) {
      return <></>;
    }

    return (
      <>
        <h2>Relation to other units</h2>
        <ul>
          {data.map(item => {
            return (
              <li>
                {item["1"]} - {item["2"]}
              </li>
            );
          })}
        </ul>
      </>
    );
  };

  const NearbySameProducts = ({ id }) => {
    const [distance, setDistance] = useState(10);
    const [nearbyUnits, setNearbyUnits] = useState(null);

    const getNearbyProducts = id => {
      fetchUtils
        .fetchJson(
          `${config.apiUrl}/product_units/${id}/nearby_same_product/${distance}`,
          {
            method: "GET"
          }
        )
        .then(res => {
          setNearbyUnits(res.json.data);
        })
        .catch(() => {
          alert("error");
        });
    };
    if (nearbyUnits === null) {
      getNearbyProducts(id);
    }

    return (
      <>
        <h2>Near-by units of the same product</h2>
        Search distance:{" "}
        <input
          id="distance_input"
          number
          onChange={d => {
            const v = parseFloat(
              document.getElementById("distance_input").value
            );
            console.log("NOOOOOOOOOOOOOOOO", v);
            setDistance(v);
            getNearbyProducts(id);
          }}
        />
        <ul>
          {nearbyUnits &&
            nearbyUnits.map(item => {
              return <li>{item}</li>;
            })}
        </ul>
      </>
    );
  };

  return (
    <>
      <Edit {...props}>
        <Grid container spacing={3}>
          <Grid item>
            <ProductUnitForm edit {...props} />
          </Grid>
          <Grid item>
            <OverlappingUnitsComponent ids={overlappingUnits} />
            <SpatialRelationsComponent data={spatialRelations} />
            <NearbySameProducts id={props.id} />
          </Grid>
        </Grid>
      </Edit>
    </>
  );
};
