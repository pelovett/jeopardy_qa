from transformers import pipeline
import torch


def main():
    questions = []
    correct = []
    ranks = []
    with open("answers.txt", "r") as in_file:
        for line in in_file:
            cur = line.strip().split('\t')
            questions.append(cur[0])
            correct.append(cur[1])
            ranks.append([x for x in cur[2:]])

    feature_extractor = pipeline("feature-extraction",
                                 model="bert-base-uncased",
                                device=1)
    num_correct = 0
    for i in range(len(questions)):
        #print(questions[i])
        #print(correct[i])
        #for j in range(len(ranks[i])):
        #    print("  "+ranks[i][j])
        #print([questions[i]] + ranks[i])
        question_rep = torch.tensor(feature_extractor(questions[i]))
        scores = []
        for j in range(len(ranks[i])):
            scores.append(
                torch.nn.functional.cosine_similarity(
                    question_rep[0, 0, :],
                    torch.tensor(feature_extractor(ranks[i][j]))[0, 0, :],
                    dim=0))
        best = scores.index(max(scores))
        if ranks[i][best] in set(correct[i].split("|")):
            num_correct += 1
    print(f'result: {num_correct} / {len(questions)}')


if __name__ == "__main__":
    main()
