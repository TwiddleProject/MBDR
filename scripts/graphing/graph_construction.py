import pandas as pd
from pandas.api.types import is_numeric_dtype
import os
import seaborn as sns
import matplotlib.pyplot as plt
from matplotlib import cm
from matplotlib.ticker import LinearLocator, FormatStrFormatter
from mpl_toolkits.mplot3d import Axes3D

FILE = "results/construction.csv"
DATA_DIR = 'results/'

def process_data(filename, input_file_param_name, constructor_param_name):
    raw = pd.read_csv(os.path.join(DATA_DIR, filename))
    # Split text file name into values
    split_ranks = raw[input_file_param_name].str.replace(".txt", "").str.split("(?<=\d)x(?=\d)")
    split_df = pd.DataFrame(split_ranks.tolist(), columns=['rank', 'rank_size'])
    df = pd.concat([raw, split_df], axis=1)
    if not is_numeric_dtype(df['Score']):
        df['Score'] = df['Score'].str.replace(",", ".")
    df['Score'] = df['Score']/1e3
    df.rename(columns = {constructor_param_name:'Algorithm'}, inplace=True)
    df[['rank', 'rank_size', 'Score']] = df[['rank', 'rank_size', 'Score']].apply(pd.to_numeric)
    df['formulas'] = df['rank'] * df['rank_size']
    return df

def plot_data(data_frame, output_file, x,y, title, xlab, ylab, legend=None):
    print("Plotting data...")
    plt.figure(dpi = 300) 
    with plt.style.context(['science', 'std-colors']):
        # Plot lines for each algorithm
        res = sns.lineplot(
            x=x, 
            y=y, 
            hue='Algorithm', 
            data=data_frame
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
        plt.savefig(os.path.join(graph_dir, output_file))
        
def plot_data_3d(data_frame, output_file, algorithm, x,y,z, title, xlab, ylab ,zlab):
    print("Plotting data...")
    data_frame = data_frame[data_frame['Algorithm'] == algorithm]
    with plt.style.context(['science', 'std-colors']):
        # Plot lines for each algorithm
        fig = plt.figure(dpi = 300) 
        ax = Axes3D(fig)
        ax.plot_trisurf(data_frame[x], data_frame[y], data_frame[z], cmap=cm.jet, linewidth=0.2)
        # ax.set_zlim(-1.01, 1.01)

        ax.zaxis.set_major_locator(LinearLocator(10))
        ax.zaxis.set_major_formatter(FormatStrFormatter('%.02f'))
        # Set labels
        ax.set_xlabel(xlab)
        ax.set_ylabel(ylab)
        ax.set_zlabel(zlab)
        ax.set_title(title)
        ax.dist = 13
        # Save plot
        print("Saving plot...")
        graph_dir = os.path.join(DATA_DIR, 'graphs')
        os.makedirs(graph_dir, exist_ok=True)
        plt.savefig(os.path.join(graph_dir, output_file))

def main():
    data_frame = process_data(
        filename="construction.csv",
        input_file_param_name="Param: knowledgeBaseFileName",
        constructor_param_name="Param: constructorClassName"
    )
    plot_data(
        data_frame=data_frame,
        title="Time vs Formula Count",
        x="formulas",
        y="Score",
        xlab="Total Formulas",
        ylab="Time (s)",
        output_file="formulas.png"
    )
    plot_data(
        data_frame=data_frame,
        title="Time vs Ranks",
        x="rank",
        y="Score",
        xlab="Ranks",
        ylab="Time (s)",
        output_file="ranks.png"
    )
    plot_data(
        data_frame=data_frame,
        title="Time vs Rank Size",
        x="rank_size",
        y="Score",
        xlab="Rank Size",
        ylab="Time (s)",
        output_file="rank_size.png"
    ) 
    plot_data_3d(
        data_frame=data_frame,
        title="Time vs Rank Size",
        algorithm = "com.mbdr.formulabased.construction.BaseRank",
        x="rank_size",
        y="rank",
        z="Score",
        xlab="Rank Size",
        ylab="Rank Count",
        zlab="Time (s)",
        output_file="baserank-3d.png"
    )
    plot_data_3d(
        data_frame=data_frame,
        title="Time vs Rank Size",
        algorithm = "com.mbdr.modelbased.construction.CumulativeFormulaRank",
        x="rank_size",
        y="rank",
        z="Score",
        xlab="Rank Size",
        ylab="Rank Count",
        zlab="Time (s)",
        output_file="cumulative-3d.png"
    )

if __name__ == '__main__':
    main()

