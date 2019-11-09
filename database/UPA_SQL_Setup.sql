-- UPA Database setup and seed script 

--TODO add boxes, add product sizes, add order, add seeding, remove shleves table and have numbering instead
--okay maybe no 3d stuff

-- Drop tables before setup

DROP TABLE Categories CASCADE CONSTRAINTS;
DROP TABLE Products CASCADE CONSTRAINTS;
DROP TABLE StorageFacilities CASCADE CONSTRAINTS;
DROP TABLE StorageRows CASCADE CONSTRAINTS;
DROP TABLE StorageShelves CASCADE CONSTRAINTS;
DROP TABLE StorageSpaces CASCADE CONSTRAINTS;
DROP TABLE StorageRooms CASCADE CONSTRAINTS;
DROP TABLE ProductUnits CASCADE CONSTRAINTS;
DROP TABLE ShippingBoxes CASCADE CONSTRAINTS;
DROP TABLE Orders CASCADE CONSTRAINTS;
DROP TABLE OrderItems CASCADE CONSTRAINTS;

-- Tables setup
-- Products and categories
CREATE TABLE Categories (
   category_id INTEGER PRIMARY KEY,
   category_name VARCHAR(128),
   parent_category_id INTEGER NULL,
   CONSTRAINT FK_ParentCategory FOREIGN KEY (parent_category_id) 
   REFERENCES Categories(category_id)
);

CREATE TABLE Products (
    product_id INTEGER PRIMARY KEY,
    product_name VARCHAR(128),
    image ORDSYS.ORDImage,
    image_si ORDSYS.SI_StillImage,
    image_ac ORDSYS.SI_AverageColor,
    image_ch ORDSYS.SI_ColorHistogram,
    image_pc ORDSYS.SI_PositionalColor,
    image_tx ORDSYS.SI_Texture,
    --category_id INTEGER,
    --price DECIMAL(8,4),
    width INTEGER,
    length INTEGER,
    height INTEGER
    --CONSTRAINT FK_Category FOREIGN KEY (category_id)
    --REFERENCES Categories(category_id)
);

-- Storage
CREATE TABLE StorageFacilities (
    facility_id INTEGER PRIMARY KEY,
    facility_name VARCHAR(128),
    city VARCHAR(128),
    street VARCHAR(128),
    house_number VARCHAR(32),
    zip_code VARCHAR(32),
    facility_geometry SDO_GEOMETRY
);

CREATE TABLE StorageRooms (
    room_id INTEGER PRIMARY KEY,
    facility_id INTEGER,
    room_geometry SDO_GEORASTER,
    CONSTRAINT FK_Facility FOREIGN KEY (facility_id)
    REFERENCES StorageFacilities(facility_id)
);

CREATE TABLE StorageRows (
    row_id INTEGER PRIMARY KEY,
    facility_id INTEGER,
    row_geometry SDO_GEOMETRY,
    CONSTRAINT FK_Facility FOREIGN KEY (facility_id)
    REFERENCES StorageFacilities(facility_id)
);

CREATE TABLE StorageSpaces (
    space_id INTEGER PRIMARY KEY,
    row_id INTEGER,
    shelf_order INTEGER, --from bottom upwards
    CONSTRAINT FK_Row FOREIGN KEY (row_id)
    REFERENCES StorageRows(row_id)
);

-- Orders

CREATE TABLE Orders (
  order_id INTEGER PRIMARY KEY,
  date_created DATE,
  date_completed DATE
);

CREATE TABLE OrderItems (
    item_id INTEGER PRIMARY KEY,
    order_id INTEGER,
    product_id INTEGER,
    quantity INTEGER,
    CONSTRAINT FK_Order FOREIGN KEY (order_id)
                        REFERENCES Orders(order_id),
    CONSTRAINT FK_Product FOREIGN KEY (product_id)
                        REFERENCES Products(product_id)
);

-- Product storage

CREATE TABLE ProductUnits (
    unit_id INTEGER PRIMARY KEY,
    product_type INTEGER,
    checked_in DATE,
    checked_out DATE NULL,
    placement INTEGER,
    assigned_order INTEGER NULL,
    geometry SDO_GEOMETRY,
    CONSTRAINT FK_Placement FOREIGN KEY (placement)
    REFERENCES StorageSpaces(space_id),
    CONSTRAINT FK_Assignment FOREIGN KEY (assigned_order)
    REFERENCES Orders(order_id)
);

-- Boxes

CREATE TABLE ShippingBoxes (
    box_id INTEGER PRIMARY KEY,
    width INTEGER,
    length INTEGER,
    height INTEGER
);

-- Define base area
-- NOTE: Probably need this for all geometries?
DELETE FROM user_sdo_geom_metadata;
INSERT INTO user_sdo_geom_metadata
    (TABLE_NAME,
     COLUMN_NAME,
     DIMINFO,
     SRID)
  VALUES (
  'StorageFacilities',
  'geometry',
  SDO_DIM_ARRAY(
    SDO_DIM_ELEMENT('X', 0, 200, 0.1),
    SDO_DIM_ELEMENT('Y', 0, 200, 0.1)
     ),
  NULL   -- SRID
);

-- Seeding
-- Products

-- Storage facility
INSERT INTO StorageFacilities VALUES (1, 'Facility 1', 'City', 'Street', '42/24', '35128',
                                      SDO_GEOMETRY(2003, NULL, NULL,
                                          SDO_ELEM_INFO_ARRAY(1,1003,3),
                                          SDO_ORDINATE_ARRAY(0,0, 200,200)
                                          )
                                      );

-- Storage rooms
INSERT INTO StorageRooms VALUES (1,1,
                                 SDO_GEOMETRY(2003, NULL, NULL,
                                    SDO_ELEM_INFO_ARRAY(1,1003,3),
                                    SDO_ORDINATE_ARRAY(10,10, 50,60)
                                    )
                                );

INSERT INTO StorageRooms VALUES (2,1,
                                    SDO_GEOMETRY(2003, NULL, NULL,
                                        SDO_ELEM_INFO_ARRAY(1,1003,1),
                                        SDO_ORDINATE_ARRAY(30,40, 50,40, 80,90, 10,90, 10,60, 50,60)
                                        )
                                 );

INSERT INTO StorageRooms VALUES (3,1,
                                    SDO_GEOMETRY(2003, NULL, NULL,
                                        SDO_ELEM_INFO_ARRAY(1,1005,4, 1,2,1, 3,2,2, 8,2,1, 10,2,1),
                                        SDO_ORDINATE_ARRAY(50,40, 120,40,130,40,120,80, 80,80, 50,40)
                                        )
                                 );

INSERT INTO StorageRooms VALUES (4,1,
                                    SDO_GEOMETRY(2004, NULL, NULL,
                                        SDO_ELEM_INFO_ARRAY(1,1003,3, 5,2003,4),
                                        SDO_ORDINATE_ARRAY(50,10, 110,40, 95,20, 100,25, 95,30)
                                        )
                                );

-- Storage rows
INSERT INTO StorageRows VALUES (1, 1,
                                SDO_GEOMETRY(2003, NULL, NULL,
                                    SDO_ELEM_INFO_ARRAY(1,1003,3),
                                    SDO_ORDINATE_ARRAY(10,10, 20,50)
                                    )
                               );

INSERT INTO StorageRows VALUES (2,1,
                                SDO_GEOMETRY(2003, NULL, NULL,
                                    SDO_ELEM_INFO_ARRAY(1,1003,3),
                                    SDO_ORDINATE_ARRAY(30,10, 40,50)
                                    )
                                );

-- Storage spaces
-- Row 1 Shelf 1
INSERT INTO StorageSpaces VALUES (1,1,1);
INSERT INTO StorageSpaces VALUES (2,1,1);
INSERT INTO StorageSpaces VALUES (3,1,1);
INSERT INTO StorageSpaces VALUES (4,1,1);
-- Row 1 Shelf 2
INSERT INTO StorageSpaces VALUES (5,1,2);
INSERT INTO StorageSpaces VALUES (6,1,2);
INSERT INTO StorageSpaces VALUES (7,1,2);
INSERT INTO StorageSpaces VALUES (9,1,2);
-- Row 1 Shelf 3
INSERT INTO StorageSpaces VALUES (10,1,3);
INSERT INTO StorageSpaces VALUES (11,1,3);
INSERT INTO StorageSpaces VALUES (12,1,3);
INSERT INTO StorageSpaces VALUES (13,1,3);

-- Row 2 Shelf 1
INSERT INTO StorageSpaces VALUES (14,2,1);
INSERT INTO StorageSpaces VALUES (15,2,1);
INSERT INTO StorageSpaces VALUES (16,2,1);
INSERT INTO StorageSpaces VALUES (17,2,1);
-- Row 2 Shelf 2
INSERT INTO StorageSpaces VALUES (18,2,2);
INSERT INTO StorageSpaces VALUES (19,2,2);
INSERT INTO StorageSpaces VALUES (20,2,2);
INSERT INTO StorageSpaces VALUES (21,2,2);
-- Row 2 Shelf 3
INSERT INTO StorageSpaces VALUES (22,2,3);
INSERT INTO StorageSpaces VALUES (23,2,3);
INSERT INTO StorageSpaces VALUES (24,2,3);
INSERT INTO StorageSpaces VALUES (25,2,3);

