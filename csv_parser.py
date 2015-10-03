import os
import sys
import csv

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

GROUP_OF_INTEREST = 'g3'

position_scores = {'1st': 4, '2nd': 3, '3rd': 2, '4th': 1}

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

def analyze_positions(vs_group):
	
	print vs_group
	side_score = []
	across_score = []

	percent_of_rats_caught_side = []
	percent_of_rats_caught_across = []

	filename = sys.argv[1]
	csv_infile = open_csv_file(filename)
	rows = next_game(csv_infile)
	while rows != None:
			
		if GROUP_OF_INTEREST not in [row[GROUP] for row in rows]:
			rows = next_game(csv_infile)
			continue

		if vs_group not in [row[GROUP] for row in rows]:
			rows = next_game(csv_infile)
			continue

		group_order = [row[GROUP] for row in rows]
		scores = [int(row[SCORE]) for row in rows]
		positions = [row[PLACE] for row in rows]
		
		our_position = group_order.index(GROUP_OF_INTEREST)
		their_position = group_order.index(vs_group)

		if abs(our_position - their_position) == 1 or abs(our_position - their_position) == 3:
			side_score.append(position_scores[positions[our_position]])
			percent_of_rats_caught_side.append(float(scores[our_position])/sum(scores))
		else:
			across_score.append(position_scores[positions[our_position]])
			percent_of_rats_caught_across.append(float(scores[our_position])/sum(scores))

		rows = next_game(csv_infile)	

	return(side_score, across_score, percent_of_rats_caught_side, percent_of_rats_caught_across)

	

def read_games():

	groups = ['g1', 'g2', 'g4', 'g6', 'g7', 'g8', 'g9']
	for group in groups:
			results = analyze_positions(group)
			with open('csv/' + group + '-6.txt', 'w') as outfile:
				outfile.write('Total side score = ' + str(sum(results[0])) + '\n')
				if len(results[0]) > 0:
					outfile.write('Average side score = ' + str(float(sum(results[0]))/len(results[0])))
				outfile.write('Side score breakdown = ' + str(results[0]) + '\n')
				outfile.write('\n')


				outfile.write('Total across score = ' + str(sum(results[1])) + '\n')
				if len(results[1]) > 0:
					outfile.write('Average across score = ' + str(float(sum(results[1]))/len(results[1])))
				outfile.write('Side across breakdown = ' + str(results[1]) + '\n')
				outfile.write('\n')
				
				if(len(results[2])) != 0:
					outfile.write('Side score avg percent = ' + str(sum(results[2])/float(len(results[2]))) + '\n')
				
				if(len(results[3])) != 0:
					outfile.write('Across score avg percent = ' + str(sum(results[3])/float(len(results[3]))) + '\n')
				
				outfile.write('Side score percent breakdown = ' + str(results[2]) + '\n')
				outfile.write('Across score percent breakdown = ' + str(results[3]) + '\n')


read_games()