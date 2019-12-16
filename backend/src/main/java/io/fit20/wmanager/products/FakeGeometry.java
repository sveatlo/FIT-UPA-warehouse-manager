package io.fit20.wmanager.products;


import oracle.spatial.geometry.JGeometry;

class FakeGeometry {
    public String type;
    public Double radius;
    public Double width;
    public Double height;
    public Double x;
    public Double y;

    public FakeGeometry(){ }
    public FakeGeometry(Double x, Double y, double radius){
        this.type = "circle";
        this.radius = radius;
        this.x = x == null ? 0 : x;
        this.y = y == null ? 0 : y;
    }
    public FakeGeometry(Double x, Double y, double width, double height) {
        this.type = "rectangle";
        this.width = width;
        this.height = height;
        this.x = x == null ? 0 : x;
        this.y = y == null ? 0 : y;
    }

    public JGeometry toJGeometry() throws Exception {
        if (this.type == "") {
            return null;
        }

        System.out.println("toJGeometry x" + x);
        System.out.println("toJGeometry y" + y);
        System.out.println("toJGeometry width" + width);
        System.out.println("toJGeometry height" + height);
        System.out.println("toJGeometry radius" + radius);
        switch (this.type) {
            case "circle":
                return JGeometry.createCircle( x + this.radius, y + this.radius, this.radius, 0);

            case "rectangle":
                double[] coords = new double[]{x,y, x+this.width,y,x+ this.width,y+this.height, x,this.height+y};
                return JGeometry.createLinearPolygon(coords, 2, 0);

            default:
                throw new Exception("unsupported geometry type");
        }
    }
}