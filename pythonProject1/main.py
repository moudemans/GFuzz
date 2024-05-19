# This is a sample Python script.
import math
from pathlib import Path

# Press Shift+F10 to execute it or replace it with your code.
# Press Double Shift to search everywhere for classes, files, tool windows, actions, and settings.

import matplotlib.pyplot as plt
import numpy as np


def print_hi(name):
    # Use a breakpoint in the code line below to debug your script.
    print(f'Hi, {name}')  # Press Ctrl+F8 to toggle the breakpoint.


def print_plot(title, axis_labels, data, legend):

    plt.title(title)
    plt.xlabel(axis_labels[0])
    plt.ylabel(axis_labels[1])

    min_y = 70
    max_y =100
    for i in range(len(data[0])):
        x = data[0][i]
        y = data[1][i]

        if len(x) ==0:
            continue

        print ("data " + str(i))
        print("\t" + str(x))
        print("\t" + str(y))

        min_y = min((int(y[0]/10 -0.5 ) ) *10, min_y)

        if (i < 0):
            plt.plot(x, y,            linestyle = 'dashed',  linewidth = 0.5)
        else:
            plt.plot(x, y, linewidth = 2)

    # plotting the points
    plt.legend(legend, loc="lower right")

    new_list = range(math.floor(min_y), math.ceil(max_y))

    plt.yticks(np.arange(min_y, max_y +1, 10))

    # giving a title to my graph

    # function to show the plot
    plt.show()


# Press the green button in the gutter to run the script.
def getData(dir, program):
    my_file = Path(dir + "/" + program + '.txt')
    if not my_file.is_file():
        return [[1]],[[80]]
    # Using readlines()
    file1 = open(dir + "/" + program + '.txt', 'r')
    Lines = file1.readlines()

    count = 0

    x_all = []
    y_all = []

    in_run = False
    # Strips the newline character
    for line in Lines:


        if line.startswith("saved_input"):
            x_list = []
            y_list = []
            in_run = True
            continue

        if in_run:
            if "," in line:
                tmp = line.split(",")
                tmp_x = int(tmp[0].strip())
                tmp_y = int(tmp[1].replace("%", "").strip())
                x_list.append(tmp_x)
                y_list.append( tmp_y)
            else:
                in_run = False
                x_all.append(x_list)
                y_all.append(y_list)

    if in_run:
        x_all.append(x_list)
        y_all.append(y_list)

    return x_all, y_all


def extend_lists(xy_data, max_trials):
    new_xy = []

    all_x = []
    all_y = []
    for i in range(len(xy_data[0])):
        x = xy_data[0][i]
        y = xy_data[1][i]
        index = 0
        new_x = []
        new_y = []
        for j in range(1, max_trials):
            new_x.append(j)
            if index < len(x) -1 and j >= x[index+1]:
                index += 1
            if index >= len(y):
                break
            new_y.append(y[index])

        all_x.append(new_x)
        all_y.append(new_y)

    for i in range(len(all_x)):
        print ("at:  ", i )
        print ("\t" + str(all_x[i]))
        print ("\t" + str(all_y[i]))

    return [all_x, all_y]

def averageData(extended_data):

    avg_x = []
    avg_y = []
    for i in range(1,len(extended_data[0][0])):
        avg_x.append(i)
        sum = 0
        for j in range(len(extended_data[1])):
            sum += extended_data[1][j][i-1]
        avg = sum / len(extended_data[1])
        avg_y.append(avg)

    return [avg_x,avg_y]


def get_max_trials(P1_x, P1_x_rand):
    max_trials = 0
    for i in range(len(P1_x)):
        if len(P1_x[i]) == 0:
            continue
        if P1_x[i][-1] > max_trials:
            max_trials = P1_x[i][-1]
    for i in range(len(P1_x_rand)):
        if len(P1_x_rand[i]) == 0:
            continue
        if P1_x_rand[i][-1] > max_trials:
            max_trials = P1_x_rand[i][-1]
    return  max_trials


if __name__ == '__main__':
    programs = ["P1", "P2", "P3", "P5", "P6", "P7"]
    average_trial_count = {"P1": 24000,"P2": 24000,"P3": 1500,"P4": 1,"P5": 1700,"P6": 70000,"P7": 2500 }
    for p in programs:
        P1_x, P1_y = getData("gfuzz", p)
        P1_x_rand, P1_y_rand = getData("random", p)

        max_trials = get_max_trials(P1_x, P1_x_rand)
        # max_trials = int(max_trials * 1.2)
        avg_trials = average_trial_count[p]
        diff = avg_trials - max_trials
        off_set = min(max(1000, min(int(diff *0.1), 5000)), avg_trials)
        max_trials += avg_trials

        extended_data = extend_lists([P1_x, P1_y], max_trials)
        extended_data_random = extend_lists([P1_x_rand, P1_y_rand], max_trials)

        Average = averageData(extended_data)
        Average_rand = averageData(extended_data_random)

        print ("Average: " )
        print ("\t" + str(Average[0]))
        print ("\t" + str(Average[1]))

        all_x_data = [Average[0], Average_rand[0]]
        # for l in extended_data[0]:
        #     all_x_data.append(l)
        all_y_data = [Average[1], Average_rand[1]]
        # for l in extended_data[1]:
        #     all_y_data.append(l)


        print_plot(p + " - Coverage Results", ["Trials","Coverage (%)"], [all_x_data, all_y_data], ["GFuzz","Random","Run 2","Run 3","Run 4"])

# See PyCharm help at https://www.jetbrains.com/help/pycharm/
