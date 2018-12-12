#!/usr/bin/env python
"""Usage: rename.py <service-name>"""
from __future__ import print_function
import os.path
import re
import sys
from subprocess import check_call, check_output

def rename(name, notify_id):
    ldashed_name = name.lower()
    humanized_name = ' '.join([x.capitalize() for x in name.split('-')])
    camel_name = ''.join([x.capitalize() for x in name.split('-')])
    all_lower_name = ldashed_name.replace('-', '')

    print("Dashed name:", ldashed_name)
    print("Human name: ", humanized_name)
    print("Camel name: ", camel_name)
    print("Package:    ", all_lower_name)
    print("Notify ID:  ", notify_id)

    files = [x for x in check_output('git ls-files', shell=True).splitlines() if x != 'rename.py']
    for path in files:
        new_path = path.replace('hello-world', ldashed_name)
        new_path = new_path.replace('HelloWorld', camel_name)
        new_path = new_path.replace('helloworld', all_lower_name)
        if new_path != path:
            check_call("mkdir -p {}".format(os.path.dirname(new_path)), shell=True)
            check_call("git mv {} {}".format(path, new_path), shell=True)
        print(new_path)

        contents = check_output(['sed', '/rename-remove-begin/,/rename-remove-end/d', new_path])
        contents = contents.replace('hello-world', ldashed_name)
        contents = contents.replace('Hello World', humanized_name)
        contents = contents.replace('HelloWorld', camel_name)
        contents = contents.replace('helloworld', all_lower_name)
        contents = contents.replace('Y2lzY29zcGFyazovL3VzL1JPT00vZmIyYWYyYTAtZmFkNi0xMWU2LWE4MzctZmQ5MjFlYjIzZDA5', notify_id)

        with open(new_path, 'w') as f:
            f.write(contents)
        check_call("git add {0}".format(new_path), shell=True)
        

if __name__ == '__main__':
    name = None
    alt_id = None
    if len(sys.argv) > 1:
        name = sys.argv[1]
    if len(sys.argv) > 2:
        alt_id = sys.argv[2]
    rename(name, alt_id)

