import pandas as pd
from pandas.api.types import is_numeric_dtype
import os
import seaborn as sns
import matplotlib.pyplot as plt
from matplotlib import cm
from matplotlib.ticker import LinearLocator, FormatStrFormatter
from mpl_toolkits.mplot3d import Axes3D

FILE = "results/entailment_regular_modelrank_old.csv"
DATA_DIR = 'results/'


def process_data(filename, input_file_param_name, reasoner_param_name):
    print('processing')
    raw = pd.read_csv(os.path.join(DATA_DIR, filename))
    # Split text file name into values
    split_ranks = raw[input_file_param_name].str.replace(
        ".txt", "").str.split("(?<=\d)x(?=\d)")
    split_df = pd.DataFrame(split_ranks.tolist(),
                            columns=['rank', 'rank_size'])
    df = pd.concat([raw, split_df], axis=1)

    if not is_numeric_dtype(df['Score']):
        df['Score'] = df['Score'].str.replace(",", ".")
    df['Score'] = df['Score']/1e3
    df.rename(columns={reasoner_param_name: 'Algorithm'}, inplace=True)
    df[['rank', 'rank_size', 'Score']] = df[[
        'rank', 'rank_size', 'Score']].apply(pd.to_numeric)
    df['formulas'] = df['rank'] * df['rank_size']
    print(df)
    df.loc[df.Algorithm == 'com.mbdr.formulabased.reasoning.RationalRegularReasoner',
           'Algorithm'] = 'RationalClosure'
    df.loc[df.Algorithm == 'com.mbdr.modelbased.reasoning.RationalModelReasoner',
           'Algorithm'] = 'ModelSatisfaction'
    return df


def plot_data(data_frame, output_file, x, y, title, xlab, ylab, legend=None):
    print("Plotting data...")
    plt.figure(dpi=600)
    with plt.style.context(['science', 'std-colors']):
        # Plot lines for each algorithm
        res = sns.lineplot(
            x=x,
            y=y,
            hue='Algorithm',
            data=data_frame,
            ci=None  # remove confidence interval
        )
        # Set labels
        res.set_xlabel(xlab)
        res.set_ylabel(ylab)
        res.set_title(title)
        if legend is not None:
            plt.legend(title=legend)
        # Save plot
        print("Saving plot...")
        graph_dir = os.path.join(DATA_DIR, 'graphs')
        os.makedirs(graph_dir, exist_ok=True)
        plt.savefig(os.path.join(graph_dir, output_file), format='eps')


def plot_data_3d(data_frame, output_file, algorithm, x, y, z, title, xlab, ylab, zlab):
    print("Plotting data...")
    data_frame = data_frame[data_frame['Algorithm'] == algorithm]
    with plt.style.context(['science', 'std-colors']):
        SMALL_SIZE = 8
        MEDIUM_SIZE = 10
        BIGGER_SIZE = 12

        plt.rc('font', size=SMALL_SIZE)          # controls default text sizes
        plt.rc('axes', titlesize=SMALL_SIZE)     # fontsize of the axes title
        # fontsize of the x and y labels
        plt.rc('axes', labelsize=MEDIUM_SIZE)
        plt.rc('xtick', labelsize=SMALL_SIZE)    # fontsize of the tick labels
        plt.rc('ytick', labelsize=SMALL_SIZE)    # fontsize of the tick labels
        plt.rc('legend', fontsize=SMALL_SIZE)    # legend fontsize
        plt.rc('figure', titlesize=BIGGER_SIZE)  # fontsize of the figure title

        # Plot lines for each algorithm
        fig = plt.figure(dpi=600)
        ax = fig.add_subplot(projection='3d')
        ax.plot_trisurf(data_frame[x], data_frame[y],
                        data_frame[z], cmap=cm.coolwarm, linewidth=0.2)

        ax.zaxis.set_major_locator(LinearLocator(11))
        ax.zaxis.set_major_formatter(FormatStrFormatter('%.02f'))
        # Set labels
        ax.set_xlabel(xlab)
        ax.set_ylabel(ylab)
        ax.set_zlabel(zlab)

        ax.dist = 10
        ax.view_init(elev=15, azim=-55)
        # Save plot
        # print("Saving plot...")
        graph_dir = os.path.join(DATA_DIR, 'graphs')
        os.makedirs(graph_dir, exist_ok=True)
        plt.savefig(os.path.join(graph_dir, output_file), format='eps')
        # plt.show()


def main():
    data_frame = process_data(
        filename="entailment_regular_modelrank_old.csv",
        input_file_param_name="Param: knowledgeBaseFileName",
        reasoner_param_name="Param: reasonerClassName"
    )
    print(data_frame)
    plot_data(
        data_frame=data_frame,
        title="Time vs Formula Count",
        x="formulas",
        y="Score",
        xlab="Total Formulas",
        ylab="Time (s)",
        output_file="formulas_entailment_regular_modelrank.eps"
    )
    plot_data(
        data_frame=data_frame,
        title="Time vs Ranks",
        x="rank",
        y="Score",
        xlab="Ranks",
        ylab="Time (s)",
        output_file="ranks_entailment_regular_modelrank.eps"
    )
    plot_data(
        data_frame=data_frame,
        title="Time vs Rank Size",
        x="rank_size",
        y="Score",
        xlab="Rank Size",
        ylab="Time (s)",
        output_file="rank_size_entailment_regular_modelrank.eps"
    )
    plot_data_3d(
        data_frame=data_frame,
        title="Time vs Rank Size",
        algorithm="RationalClosure",
        x="rank_size",
        y="rank",
        z="Score",
        xlab="Rank Size",
        ylab="Rank Count",
        zlab="Time (s)",
        output_file="rationalRegular-3d_entailment_regular_modelrank.eps"
    )
    plot_data_3d(
        data_frame=data_frame,
        title="Time vs Rank Size",
        algorithm="ModelSatisfaction",
        x="rank_size",
        y="rank",
        z="Score",
        xlab="Rank Size",
        ylab="Rank Count",
        zlab="Time (s)",
        output_file="modelReasoner-3d_entailment_regular_modelrank.eps"
    )


if __name__ == '__main__':
    main()
