import React, { useState } from "react";
import {
  Datagrid,
  List,
  ReferenceField,
  Responsive,
  DateField,
  SimpleList,
  TextField
} from "react-admin";
import Box from "@material-ui/core/Box";
import Grid from "@material-ui/core/Grid";
import Card from "@material-ui/core/Card";
import CardContent from "@material-ui/core/CardContent";
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
      <Grid container spacing={5}>
        <Grid item xs={6} sm={4}>
          <Card>
            <CardContent>
              Total area used by stored units: {totalArea.toFixed(2)}
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={6} sm={4}>
          <Card>
            <CardContent>
              Most space-consuming product: {mostSpaceConsumingProduct}
              <br />
              Area consumed {mostSpaceConsumingProductArea.toFixed(2)}
            </CardContent>
          </Card>
        </Grid>
      </Grid>
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
              <DateField source="checked_in" />
              <DateField source="checked_out" />
            </Datagrid>
          }
        />
      </List>
    </>
  );
};
