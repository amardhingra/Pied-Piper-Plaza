import os
import sys
import csv

DIR = 'csv/'

# game,name,score,place,timeout
global GAME_ID
GAME_ID = 'game'
global GROUP
GROUP = 'name'
global SCORE
SCORE = 'score'
global PLACE
PLACE = 'place'
global TIMEOUT
TIMEOUT = 'timeout'

def open_csv_file(filename):
	infile = open(filename, 'rb')
	csv_infile = csv.DictReader(infile)
	csv_infile.next()
	return csv_infile


def next_game(dict_reader):
	try:
		rows = []
		for i in range(4):
			rows.append(dict_reader.next())
		return rows
	except:
		return None

def split():

	side_score = []
	across_score = []

	percent_of_rats_caught_side = []
	percent_of_rats_caught_across = []

	filename = sys.argv[1]
	csv_infile = open_csv_file(filename)
	rows = next_game(csv_infile)

	outfile_20 = open(DIR + 'player_6_pipers_20.csv', 'w')
	outfile_101 = open(DIR + 'player_6_pipers_101.csv', 'w')
	outfile_500 = open(DIR + 'player_6_pipers_500.csv', 'w')

	outfile_20.write('game,name,score,place,timeout\n')
	outfile_101.write('game,name,score,place,timeout\n')
	outfile_500.write('game,name,score,place,timeout\n')
	
	while rows != None:

		group_order = [row[GROUP] for row in rows]
		scores = [int(row[SCORE]) for row in rows]
		positions = [row[PLACE] for row in rows]

		rows2 = []
		for row in rows:
			rows2.append(row[GAME_ID] + ',' + row[GROUP] + ',' + row[SCORE] + ',' + row[PLACE] + ',' + row[TIMEOUT])
		if sum(scores) <= 20:
			outfile_20.write('\n'.join(rows2) + '\n')
		elif sum(scores) <= 101:
			outfile_101.write('\n'.join(rows2) + '\n')
		elif sum(scores) <= 500:
			outfile_500.write('\n'.join(rows2) + '\n')

		rows = next_game(csv_infile)	

	outfile_20.close()
	outfile_101.close()
	outfile_500.close()

split()