FROM node:18-bullseye-slim

WORKDIR /app

#install dependencies
COPY package*.json ./

ENV NODE_ENV=development
RUN npm ci

#copy all files (including source code) for live reloading.
COPY . .

#expose the port for the development server
EXPOSE 5173

CMD ["npm", "run", "dev"]