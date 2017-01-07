#!/usr/bin/env python2
import BaseHTTPServer, SimpleHTTPServer
import ssl

HOST = 'localhost'
PORT = 8443

httpd = BaseHTTPServer.HTTPServer((HOST, PORT), SimpleHTTPServer.SimpleHTTPRequestHandler)
httpd.socket = ssl.wrap_socket (httpd.socket, certfile='localhost.pem', server_side=True)

try:
    print 'Serving on https://%s:%s/directory.json' % (HOST, PORT)
    httpd.serve_forever()
except KeyboardInterrupt, e:
    httpd.server_close()
