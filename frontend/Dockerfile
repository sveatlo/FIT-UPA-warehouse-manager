# Build dev image
FROM node:12
# Copy files to appropriate locations
WORKDIR /app
VOLUME ["/app"]
# Install dependencies
RUN ["yarn", "install"]
# Run the application
CMD ["yarn", "start"]
