-- UPA Database setup and seed script 

-- Drop tables before setup

DROP TABLE Categories CASCADE CONSTRAINTS;
DROP TABLE Products CASCADE CONSTRAINTS;
DROP TABLE StorageFacilities CASCADE CONSTRAINTS;
DROP TABLE StorageRows CASCADE CONSTRAINTS;
DROP TABLE StorageShelves CASCADE CONSTRAINTS;
DROP TABLE StorageSpaces CASCADE CONSTRAINTS;
DROP TABLE ProductUnits CASCADE CONSTRAINTS;

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
    category_id INTEGER,
    price DECIMAL(8,4),
    CONSTRAINT FK_Category FOREIGN KEY (category_id) 
    REFERENCES Categories(category_id)
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

CREATE TABLE StorageRows (
    row_id INTEGER PRIMARY KEY,
    facility_id INTEGER,
    row_geometry SDO_GEOMETRY,
    CONSTRAINT FK_Facility FOREIGN KEY (facility_id)
    REFERENCES StorageFacilities(facility_id)
);

CREATE TABLE StorageShelves (
    shelf_id INTEGER PRIMARY KEY,
    row_id INTEGER,
    shelf_geometry SDO_GEOMETRY,
    CONSTRAINT FK_Row FOREIGN KEY (row_id)
    REFERENCES StorageRows(row_id)
);

CREATE TABLE StorageSpaces (
    space_id INTEGER PRIMARY KEY,
    shelf_id INTEGER,
    space_geometry SDO_GEOMETRY,
    CONSTRAINT FK_Shelf FOREIGN KEY (shelf_id)
    REFERENCES StorageShelves(shelf_id)
);

-- Product storage

CREATE TABLE ProductUnits (
    unit_id INTEGER PRIMARY KEY,
    product_type INTEGER,
    checked_in DATE,
    checked_out DATE NULL,
    placement INTEGER,
    CONSTRAINT FK_Placement FOREIGN KEY (placement)
    REFERENCES StorageSpaces(space_id)
);

-- Orders



-- Seeding











