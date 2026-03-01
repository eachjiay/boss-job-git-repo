# Production stage - Simple static file hosting
FROM nginx:alpine

# Copy built assets from local machine
# Ensure you run 'npm run build' locally on Windows first!
COPY dist /usr/share/nginx/html

# Copy custom Nginx configuration
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
