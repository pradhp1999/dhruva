import os

import script

def main():
	print('This is a demo to show the entry point for Tee4py.')

	print('You can make a system call using Python os module.')
	os.system('ls -al')

	script.demo1()
	script.demo2()


if __name__ == '__main__':
	main()