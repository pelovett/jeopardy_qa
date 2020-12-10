from transformers import AutoTokenizer, AutoModel
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

    documents = {}
    with open("clean_docs.txt", "r") as in_file:
        for line in in_file:
            cur = line.strip().split('\t')
            if len(cur) == 1:
                documents[cur[0]] = ''
            else:
                documents[cur[0]] = cur[1]

    tokenizer = AutoTokenizer.from_pretrained('bert-base-cased')
    model = AutoModel.from_pretrained('bert-base-cased')

    num_correct = 0
    for i in range(len(questions)):
        print(questions[i])
        print(correct[i])
        context = [documents[title] for title in ranks[i]]
        tokenized_input = tokenizer([questions[i]] + context,
                                    add_special_tokens=True,
                                    truncation=True,
                                    padding=True,
                                    max_length=128,
                                    return_tensors='pt')
        with torch.no_grad():
            model_out = model(tokenized_input['input_ids'])[0][:, 0, :].cpu()
        scores = []
        for j in range(len(ranks[i])):
            scores.append(
                torch.nn.functional.cosine_similarity(
                    model_out[0, :],
                    model_out[j+1, :],
                    dim=0).item())
        best = scores.index(max(scores))
        for j in range(len(ranks[i])):
            print("  "+ranks[i][j]+" : "+str(scores[j]))
        if ranks[i][best] in set(correct[i].split("|")):
            num_correct += 1
    print(f'result: {num_correct} / {len(questions)}')


if __name__ == "__main__":
    main()
