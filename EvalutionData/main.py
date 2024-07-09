# This is a sample Python script.
import math
import time
from pathlib import Path

import matplotlib.pyplot as plt
import numpy as np

PLOT_BASE = True
PLOT_PGFUZZ = True
PLOT_RANDOM = False
PLOT_COMPOUND = True

PLOT_BENCHMARK = {
    "P1": False,
    "P2": False,
    "P3": False,
    "P4": False,
    "P5": False,
    "P6": False,
    "P7": False,
    "P8": False,
    "P9": False,
    "A1": True,
    "A1-200": True,
    "A1-500": True,
    "A1-1000": True,
    "A1-10000": True,
    "A2": True,
    "P10": False,
}
titles = {"P10": "P8-Extended"}

plot_settings = {
    "P1": {

    },
    "A1": {
        "title": "Cycle detection (|N|=100)",
        "min_y": 70,
        "max_x": 13000,
    },

    "A1-200": {
        "title": "Cycle detection (|N|=200)",
        "min_y": 70,
        "max_x": 13000,
    },

    "A1-500": {
        "title": "Cycle detection (|N|=500)",
        "min_y": 70,
        "max_x": 13000,
    },
    "A1-1000": {
        "title": "Cycle detection (|N|=1000)",
        "min_y": 70,
        "max_x": 13000,
    },
    "A1-10000": {
        "title": "Cycle detection (|N|=10000)",
        "min_y": 70,
        "max_x": 13000,
    },
    "P10": {
        "title": "P8-Extended",
        "max_x": 30000,
        "min_y": 50,

    },
    "P8": {
        "max_x": 23000,
        "min_y": 50,

    }
}

average_trial_count = {"P1": 24000, "P2": 24000, "P3": 1500, "P4": 1, "P5": 1700, "P6": 70000, "P7": 2500, "P8": 20000,
                       "P9": 22000, "A1": 11000, "A1-200": 11000, "A1-500": 11000, "A1-1000": 11000, "A1-10000": 11000, "A2": 11000, "P10": 20000}

programs = ["P1", "P2", "P3", "P5", "P6", "P7", "P8", "P9", "A1", "A1-200", "A1-500", "A1-1000", "A1-10000", "A2",
            "P10"]


def print_plot(title, axis_labels, data, legend, colors, output_name, min_y_fixed):
    plt.title(title)
    plt.xlabel(axis_labels[0])
    plt.ylabel(axis_labels[1])

    min_y = 100
    max_y = 100
    print("****")
    for i in range(len(data[0])):
        x = data[0][i]
        y = data[1][i]

        if len(x) == 0:
            continue

        min_y = min((int(y[0] / 10 - 0.5)) * 10, min_y)

        plt.plot(x, y, colors[i], linewidth=2)

    # If min Y is fixed dont use the min y from the data
    if min_y_fixed != None:
        min_y = min_y_fixed

    plt.yticks(np.arange(min_y, max_y + 1, 10))

    # plotting the points
    plt.legend(legend, loc="lower right")

    print()
    print("Plotting ", title)
    print("min_y: ", min_y)
    print("labels: ", axis_labels)
    print("legend: ", legend)
    print()
    #
    # print("colors:")
    # print(plt[0].get_color())  # 'b'
    # print(plt[1].get_color())  # 'b'
    # print(plt[2].get_color())  # 'b'
    # print(plt[3].get_color())  # 'b'
    # function to show the plot
    plt.savefig('aafigures/zCycle' + output_name + '.pdf')
    plt.show()


# Press the green button in the gutter to run the script.
def getData(dir, program):
    my_file = Path(dir + "/" + program + '.txt')
    if not my_file.is_file():
        print("Could not find input file for: ", program)
        return [[1]], [[80]]
    # Using readlines()
    file1 = open(dir + "/" + program + '.txt', 'r')
    Lines = file1.readlines()

    x_all = []
    y_all = []

    in_run = False
    # Strips the newline character
    for line in Lines:
        if line.startswith("#"):
            continue
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
                y_list.append(tmp_y)
            else:
                in_run = False
                x_all.append(x_list)
                y_all.append(y_list)

    if in_run:
        x_all.append(x_list)
        y_all.append(y_list)

    return x_all, y_all


def extend_lists(xy_data, max_trials):
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
            if index < len(x) - 1 and j >= x[index + 1]:
                index += 1
            if index >= len(y):
                break
            new_y.append(y[index])

        all_x.append(new_x)
        all_y.append(new_y)

    return [all_x, all_y]


def averageData(extended_data):
    avg_x = []
    avg_y = []
    for i in range(1, len(extended_data[0][0])):
        avg_x.append(i)
        sum = 0
        for j in range(len(extended_data[1])):
            sum += extended_data[1][j][i - 1]
        avg = sum / len(extended_data[1])
        avg_y.append(avg)

    return [avg_x, avg_y]


def get_max_trials(x_list):
    max_trials = 0
    for i in range(len(x_list)):
        if x_list[i] is None:
            continue
        curr_x_list = x_list[i]
        for i in range(len(curr_x_list)):
            if len(curr_x_list[i]) == 0:
                continue
            if curr_x_list[i][-1] > max_trials:
                max_trials = curr_x_list[i][-1]

    return max_trials


def get_steps_x_y(all_x_data, all_y_data):
    all_step_x = []
    all_step_y = []
    for m in range(0, len(all_x_data)):
        method_step_x = []
        method_step_y = []
        prev_val = 0
        for i in range(0, len(all_y_data[m])):
            if all_y_data[m][i] != prev_val:
                prev_val = all_y_data[m][i]
                method_step_x.append(all_x_data[m][i])
                method_step_y.append(all_y_data[m][i])
            if i >= len(all_y_data[m]) - 1:
                method_step_x.append(all_x_data[m][i])
                method_step_y.append(all_y_data[m][i])
        all_step_x.append(method_step_x)
        all_step_y.append(method_step_y)
    return all_step_x, all_step_y


def set_max_trials(p, param):
    max_trials = get_max_trials(param)
    avg_trials = average_trial_count[p]
    diff = avg_trials - max_trials
    off_set = min(max(1000, min(int(diff * 0.1), 5000)), avg_trials)
    max_trials += avg_trials

    if plot_settings.get(p) is not None:
        if plot_settings.get(p).get("max_x") is not None:
            max_trials = plot_settings.get(p).get("max_x")
    return max_trials


if __name__ == '__main__':
    a_x = []
    a_y = []
    for p in programs:
        if not PLOT_BENCHMARK.get(p):
            continue
        P1_x = None
        P1_x_rand = None
        P1_x_gen = None
        P1_x_comp = None

        P1_x, P1_y = getData("gfuzz", p)
        P1_x_rand, P1_y_rand = getData("random", p)
        P1_x_gen, P1_y_gen = getData("gen", p)
        P1_x_comp, P1_y_comp = getData("compound", p)

        max_trials = set_max_trials(p, [P1_x, P1_x_rand, P1_x_gen, P1_x_comp])

        extended_data = extend_lists([P1_x, P1_y], max_trials)
        extended_data_random = extend_lists([P1_x_rand, P1_y_rand], max_trials)
        extended_data_gen = extend_lists([P1_x_gen, P1_y_gen], max_trials)
        extended_data_comp = extend_lists([P1_x_comp, P1_y_comp], max_trials)

        Average = averageData(extended_data)
        Average_rand = averageData(extended_data_random)
        Average_gen = averageData(extended_data_gen)
        Average_comp = averageData(extended_data_comp)

        if p == 'P5':
            for y in range(len(Average_rand[1])):
                Average_rand[1][y] += 0.1
            for y in range(len(Average_gen[1])):
                Average_gen[1][y] -= 0.1

        print("p: ", p)
        print("gen max avg: ", Average_gen[1][-1])
        print("pgfuzz max avg: ", Average[1][-1])


        all_x_data = []
        all_y_data = []
        legend = []
        colors = []
        if (PLOT_PGFUZZ):
            all_x_data.append(Average[0])
            all_y_data.append(Average[1])
            legend.append("PGFuzz")
            colors.append("#1f77b4")
        if (PLOT_RANDOM):
            all_x_data.append(Average_rand[0])
            all_y_data.append(Average_rand[1])
            legend.append("Random")
            colors.append("#ff7f0e")

        if (PLOT_BASE):
            all_x_data.append(Average_gen[0])
            all_y_data.append(Average_gen[1])
            colors.append("#2ca02c")

            if p == "P1" or "A1" in p:
                legend.append("GMark")
            else:
                legend.append("PGMark")
        if PLOT_COMPOUND:
            all_x_data.append(Average_comp[0])
            all_y_data.append(Average_comp[1])
            legend.append("Compound")
            colors.append("#d62728")

        all_x_data_steps, all_y_data_steps = get_steps_x_y(all_x_data, all_y_data)

        # Prevent to many api calls
        time.sleep(0.1)

        title = p
        if plot_settings.get(p) is not None:
            if plot_settings.get(p).get("title") is not None:
                title = plot_settings.get(p).get("title")

        min_y_fixed = None
        if plot_settings.get(p) is not None:
            if plot_settings.get(p).get("min_y") is not None:
                min_y_fixed = plot_settings.get(p).get("min_y")

        print_plot(title + " - Coverage Results", ["Trials", "Coverage (%)"], [all_x_data, all_y_data], legend, colors,
                   p, min_y_fixed)

# See PyCharm help at https://www.jetbrains.com/help/pycharm/
