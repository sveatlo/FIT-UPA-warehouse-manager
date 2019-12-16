import React from 'react';
import {
    Filter,
    TextInput,
} from 'react-admin';

export const SearchFilter = ({ children, ...props }) => (
    <Filter {...props}>
        <TextInput label="Search" source="q" alwaysOn />
        {children}
    </Filter>
);
