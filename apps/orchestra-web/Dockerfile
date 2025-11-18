FROM node:20.12.2-bullseye
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
EXPOSE 3000
CMD ["npm","run","start","--","--host","0.0.0.0","--port","3000"]
