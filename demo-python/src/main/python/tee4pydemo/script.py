import requests

def demo1():
	print('You can import another module and invoke it.')


def demo2():
	print('You can also put 3rd party library in requirements.txt and import it.')
	response = requests.get('https://api.ciscospark.com/v1/ping', auth=('user', 'pass'))
	print(response.json())
