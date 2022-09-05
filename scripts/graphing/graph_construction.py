from concurrent.futures import process
import pandas as pd
import os
import seaborn as sns
import matplotlib.pyplot as plt

FILE = "results/construction.csv"
DATA_DIR = 'results/'

def process_data(filename, input_file_param_name, constructor_param_name):
    raw = pd.read_csv(os.path.join(DATA_DIR, filename))
    # Split text file name into values
    split_ranks = raw[input_file_param_name].str.replace(".txt", "").str.split("(?<=\d)x(?=\d)")
    split_df = pd.DataFrame(split_ranks.tolist(), columns=['rank', 'rank_size'])
    df = pd.concat([raw, split_df], axis=1)
    df['Score'] = df['Score'].str.replace(",", ".")
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
        ylab="Time (ms)",
        output_file="formulas.png"
    )
    plot_data(
        data_frame=data_frame,
        title="Time vs Ranks",
        x="rank",
        y="Score",
        xlab="Ranks",
        ylab="Time (ms)",
        output_file="ranks.png"
    )
    plot_data(
        data_frame=data_frame,
        title="Time vs Rank Size",
        x="rank_size",
        y="Score",
        xlab="Rank Size",
        ylab="Time (ms)",
        output_file="rank_size.png"
    )

if __name__ == '__main__':
    main()

