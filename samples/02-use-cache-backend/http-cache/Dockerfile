FROM nginx
ADD nginx.conf /etc/nginx/nginx.conf
RUN mkdir /usr/share/nginx/task-cache
RUN chown nginx /usr/share/nginx/task-cache
RUN mkdir /usr/share/nginx/task-cache-temp
RUN chown nginx /usr/share/nginx/task-cache-temp

VOLUME ["/usr/share/nginx/task-cache", "/usr/share/nginx/task-cache-temp"]
CMD ["nginx", "-g", "daemon off;"]
