import React, { useState } from "react";
import {
  Datagrid,
  List,
  ReferenceField,
  Responsive,
  SimpleList,
  TextField
} from "react-admin";
import Box from "@material-ui/core/Box";
import { fetchUtils } from "ra-core";
import config from "../../config";

export const ProductUnitsList = props => {
  const [totalArea, setTotalArea] = useState(0);
  const [mostSpaceConsumingProduct, setMostSpaceConsumingProduct] = useState(0);
  const [
    mostSpaceConsumingProductArea,
    setMostSpaceConsumingProductArea
  ] = useState(0);

  const getTotalArea = () => {
    fetchUtils
      .fetchJson(`${config.apiUrl}/warehouse/total_used_area/`, {
        method: "GET"
      })
      .then(res => {
        setTotalArea(res.json.data);
      })
      .catch(() => {
        alert("error");
      });
  };
  const getMostSpaceDemandingProduct = () => {
    fetchUtils
      .fetchJson(`${config.apiUrl}/warehouse/most_used_space/`, {
        method: "GET"
      })
      .then(res => {
        setMostSpaceConsumingProduct(res.json.data.product_id);
        setMostSpaceConsumingProductArea(res.json.data.area);
      })
      .catch(() => {
        alert("error");
      });
  };
  getTotalArea();
  getMostSpaceDemandingProduct();

  return (
    <>
      <Box bgcolor="primary.main">
        Total area used by stored units: {totalArea}
      </Box>
      <Box bgcolor="primary.main">
        Most space-consuming product: {mostSpaceConsumingProduct}
        <br />
        Area consumed {mostSpaceConsumingProductArea}
      </Box>
      <Box bgcolor="primary.main">{totalArea}</Box>
      <List {...props}>
        <Responsive
          small={<SimpleList primaryText={record => record.name} />}
          medium={
            <Datagrid rowClick="edit">
              <TextField source="id" />
              <ReferenceField
                source="product_id"
                reference="products"
                sortable={false}
              >
                <TextField source="name" />
              </ReferenceField>
            </Datagrid>
          }
        />
      </List>
    </>
  );
};
