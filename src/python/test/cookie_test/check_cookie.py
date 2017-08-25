'''
Created on 11 Jul 2017

@author: William Tucker
'''

from crypto_cookie.auth_tkt import SecureCookie

def check_cookie():
    key = 'yUGFos3E/MbDiN4q1YjjTE/gmO7SLxeetAxxAgPUaB0='
    key = key.decode('base64')
    cookie_value = 'c39193baf69307eb13967af1c6a3e9fdbbf4b468eefdb0a543ba2a28aaa74ae4f452e8482dd4b7bf465013369f92decd-8d98649db5e1ef67e287474442645cb6-25e8ec8bd024ae0dd56078403fe099e4d9e948bdf51661d848fee5e8cdb0afc1'
    
    cookie = SecureCookie.parse_ticket(key, cookie_value, None, None)
    print(cookie)

if __name__ == '__main__':
    check_cookie();