'''
Created on 3 Jul 2017

@author: William Tucker
'''

from __future__ import print_function

import os
import base64
import getopt
import logging
import sys

from crypto_cookie.encoding import Encoder
from crypto_cookie.auth_tkt import SecureCookie

log = logging.getLogger(__name__)

KEY_LENGTH = 32
DEFAULT_OUTPUT_DIRECTORY = os.path.join(os.path.dirname(os.path.realpath(__file__)), 'output/')

SECURE_COOKIE_FILE = 'secure-cookie-info'
USER_DETAILS_COOKIE_FILE = 'user-details-cookie-info'

def generate_secret_key():
    return os.urandom(KEY_LENGTH)

def encode_message(key, message):
    key = generate_secret_key()
    
    return Encoder().encode_msg(message, key)

def create_cookie(key, value, ip, tokens = (), user_data = ''):
    return SecureCookie(key, value, ip, tokens, user_data)

def main(argv):
    logging.basicConfig(level=logging.DEBUG)
    
    secure_cookie_file = None
    user_details_cookie_file = None
    try:
        output_directory = DEFAULT_OUTPUT_DIRECTORY
        options, _ = getopt.getopt(argv, "o:")
        
        for option, value in options:
            if option == '-o' and value:
                output_directory = value
        
        if not os.path.exists(output_directory):
            os.mkdir(output_directory)
        
        if not output_directory.endswith(os.path.sep):
            output_directory += os.path.sep
        
        secure_cookie_file = open(
                os.path.join(output_directory + SECURE_COOKIE_FILE), 'w')
        user_details_cookie_file = open(
                os.path.join(output_directory + USER_DETAILS_COOKIE_FILE), 'w')
        
    except getopt.GetoptError:
        print('-o <outputfile>')
        sys.exit(2)
    
    ip = '127.0.0.1'
    key = generate_secret_key()
    encoded_key = key.encode('base64')
    print('encoded_secret_key: {}'.format(encoded_key), file = secure_cookie_file)
    print('encoded_secret_key: {}'.format(encoded_key), file = user_details_cookie_file)
    log.info('encoded secret key: {}'.format(encoded_key))
    
    # Secure Cookie
    message = b'a secret message'
    print('message: {}'.format(message), file = secure_cookie_file)
    
    encoded_message = encode_message(key, message)
    print('encoded_message: {}'.format(encoded_message), file = secure_cookie_file)
    
    secure_cookie = create_cookie(key, message, ip)
    cookie_value = secure_cookie.cookie_value()
    print('cookie_value: {}'.format(cookie_value), file = secure_cookie_file)
    
    log.info('encoded secure cookie\nmessage: {}, cookie value: {}'
             .format(message, cookie_value))
    
    # User Details Cookie
    userid = 'userid'
    print('userid: {}'.format(userid), file = user_details_cookie_file)
    tokens = ('token1', 'token2')
    print('tokens: {}'.format(tokens), file = user_details_cookie_file)
    user_data = 'userdata'
    print('user_data: {}'.format(user_data), file = user_details_cookie_file)
    
    encoded_message = encode_message(key, message)
    print('encoded_message: {}'.format(encoded_message), file = user_details_cookie_file)
    
    secure_cookie = create_cookie(key, userid, ip, tokens, user_data)
    cookie_value = secure_cookie.cookie_value()
    print('cookie_value: {}'.format(cookie_value), file = user_details_cookie_file)
    
    log.info('encoded user details cookie cookie\nuserid: {}, tokens: {}, user_data: {}, cookie value: {}'
             .format(userid, tokens, user_data, cookie_value))

if __name__ == "__main__":
    main(sys.argv[1:])
