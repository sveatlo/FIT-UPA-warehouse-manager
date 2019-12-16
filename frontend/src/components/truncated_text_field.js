import React from 'react';
import PropTypes from 'prop-types';
import {
    TextField,
} from 'react-admin';

const TruncatedTextField = ({ source, maxLength, record = {}, ...props }) => {
    if (!!record[source] && record[source].length > maxLength) {
        record[source] = record[source].substr(0, maxLength) + "..."
    }

    return <TextField record={record} source={source} {...props} />
};

TruncatedTextField.propTypes = {
    label: PropTypes.string,
    record: PropTypes.object,
    source: PropTypes.string.isRequired,
    maxLength: PropTypes.number,
};

export default TruncatedTextField;
