from sklearn.feature_extraction.text import TfidfVectorizer
from os.path import dirname, join
import pandas as pd
from nltk.tokenize import TweetTokenizer,ToktokTokenizer
import re

filename = join(dirname(__file__), "Spam_data.csv")
with open (filename) as csv_file:
    df = pd.read_csv(csv_file)



df = df.where((pd.notnull),' ')
df.loc[df['spam/ham']=="spam","spam/ham"]=0
df.loc[df['spam/ham']=="ham","spam/ham"]=1
x = df['message']
y= df['spam/ham']
x_mar=df['marathi']
x_hin = df['hindi']

stop_words_marathi = ['असलेल्या', 'असा', 'असून', 'असे', 'आणि', 'आता', 'आपल्या', 'आला', 'आली', 'आले', 'आहे', 'आहेत', 'एक', 'एका', 'कमी', 'करणयात', 'करून', 'का', 'काम', 'काय', 'काही', 'किवा', 'की', 'केला', 'केली', 'केले', 'कोटी', 'गेल्या', 'घेऊन', 'जात', 'झाला', 'झाली', 'झाले', 'झालेल्या', 'टा', 'डॉ', 'तर', 'तरी', 'तसेच', 'ता', 'त्या', 'त्याचा', 'त्याची', 'त्याच्या', 'त्याना', 'त्यानी', 'त्यामुळे', 'त्री', 'दिली', 'दिलेल्या', 'दुसर्‍या', 'दोन', 'धरून', 'न', 'नाही', 'निर्ण्‍य', 'पण', 'पम', 'परयतन', 'पाटील', 'म', 'मात्र', 'माहिती', 'मी', 'मुबी', 'म्हणजे', 'म्हणाले', 'म्हणून', 'म्हणे', 'या', 'याचा', 'याची', 'याच्या', 'याना', 'यानी', 'येणार', 'येत', 'येथील', 'येथे', 'लाख', 'व', 'व्यकत', 'सर्व', 'सागित्ले', 'सुरू', 'हजार', 'हा', 'ही', 'हे', 'होणार', 'होत', 'होता', 'होती', 'होते']
hindi_stop_words = [
    'अंदर', 'अत', 'अदि', 'अप', 'अपना', 'अपनि', 'अपनी', 'अपने', 'अभि', 'अभी', 'आदि', 'आप', 'इंहिं', 'इंहें', 'इंहों', 'इतयादि', 'इत्यादि', 'इन', 'इनका', 'इन्हीं', 'इन्हें', 'इन्हों', 'इस', 'इसका', 'इसकि', 'इसकी', 'इसके', 'इसमें', 'इसि', 'इसी', 'इसे', 'उंहिं', 'उंहें', 'उंहों', 'उन', 'उनका', 'उनकि', 'उनकी', 'उनके', 'उनको', 'उन्हीं', 'उन्हें', 'उन्हों', 'उस', 'उसके', 'उसि', 'उसी', 'उसे', 'एक', 'एवं', 'एस', 'एसे', 'ऐसे', 'ओर', 'और', 'कइ', 'कई', 'कर', 'करता', 'करते', 'करना', 'करने', 'करें', 'कहते', 'कहा', 'का', 'काफि', 'काफ़ी', 'कि', 'किंहें', 'किंहों', 'कितना', 'किन्हें', 'किन्हों', 'किया', 'किर', 'किस', 'किसि', 'किसी', 'किसे', 'की', 'कुछ', 'कुल', 'के', 'को', 'कोइ', 'कोई', 'कोन', 'कोनसा', 'कौन', 'कौनसा', 'गया', 'घर', 'जब', 'जहाँ', 'जहां', 'जा', 'जिंहें', 'जिंहों', 'जितना', 'जिधर', 'जिन', 'जिन्हें', 'जिन्हों', 'जिस', 'जिसे', 'जीधर', 'जेसा', 'जेसे', 'जैसा', 'जैसे', 'जो', 'तक', 'तब', 'तरह', 'तिंहें', 'तिंहों', 'तिन', 'तिन्हें', 'तिन्हों', 'तिस', 'तिसे', 'तो', 'था', 'थि', 'थी', 'थे', 'दबारा', 'दवारा', 'दिया', 'दुसरा', 'दुसरे', 'दूसरे', 'दो', 'द्वारा', 'न', 'नहिं', 'नहीं', 'ना', 'निचे', 'निहायत', 'नीचे', 'ने', 'पर', 'पहले', 'पुरा', 'पूरा', 'पे', 'फिर', 'बनि', 'बनी', 'बहि', 'बही', 'बहुत', 'बाद', 'बाला', 'बिलकुल', 'भि', 'भितर', 'भी', 'भीतर', 'मगर', 'मानो', 'मे', 'में', 'यदि', 'यह', 'यहाँ', 'यहां', 'यहि', 'यही', 'या', 'यिह', 'ये', 'रखें', 'रवासा', 'रहा', 'रहे', 'ऱ्वासा', 'लिए', 'लिये', 'लेकिन', 'व', 'वगेरह', 'वरग', 'वर्ग', 'वह', 'वहाँ', 'वहां', 'वहिं', 'वहीं', 'वाले', 'वुह', 'वे', 'वग़ैरह', 'संग', 'सकता', 'सकते', 'सबसे', 'सभि', 'सभी', 'साथ', 'साबुत', 'साभ', 'सारा', 'से', 'सो', 'हि', 'ही', 'हुअं', 'हुआ', 'हुइ', 'हुई', 'हुए', 'हे', 'हें', 'है', 'हैं', 'हो', 'होता', 'होति', 'होती', 'होते']
stop_words = [
    'a', 'about', 'above', 'after', 'again', 'against', 'ain', 'all', 'am', 'an',
    'and', 'any', 'are', 'aren', 'aren\'t', 'as', 'at', 'be', 'because', 'been',
    'before', 'being', 'below', 'between', 'both', 'but', 'by', 'can', 'couldn',
    'couldn\'t', 'd', 'did', 'didn', 'didn\'t', 'do', 'does', 'doesn', 'doesn\'t',
    'doing', 'don', 'don\'t', 'down', 'during', 'each', 'few', 'for', 'from', 'further',
    'had', 'hadn', 'hadn\'t', 'has', 'hasn', 'hasn\'t', 'have', 'haven', 'haven\'t', 'having',
    'he', 'her', 'here', 'hers', 'herself', 'him', 'himself', 'his', 'how', 'i', 'if', 'in',
    'into', 'is', 'isn', 'isn\'t', 'it', 'it\'s', 'its', 'itself', 'just', 'll', 'm', 'ma',
    'me', 'mightn', 'mightn\'t', 'more', 'most', 'mustn', 'mustn\'t', 'my', 'myself', 'needn',
    'needn\'t', 'no', 'nor', 'not', 'now', 'o', 'of', 'off', 'on', 'once', 'only', 'or', 'other',
    'our', 'ours', 'ourselves', 'out', 'over', 'own', 're', 's', 'same', 'shan', 'shan\'t',
    'she', 'should', 'shouldn', 'shouldn\'t', 'so', 'some', 'such', 't', 'than', 'that', 'the',
    'their', 'theirs', 'them', 'themselves', 'then', 'there', 'these', 'they', 'this', 'those',
    'through', 'to', 'too', 'under', 'until', 'up', 've', 'very', 'was', 'wasn', 'wasn\'t', 'we',
    'were', 'weren', 'weren\'t', 'what', 'when', 'where', 'which', 'while', 'who', 'whom', 'why',
    'will', 'with', 'won', 'won\'t', 'wouldn', 'wouldn\'t', 'y', 'you', 'your', 'yours', 'yourself',
    'yourselves'
]

def tokenizer(text):
    tok = TweetTokenizer()
    return tok.tokenize(text)
def tokenizer_2(text):
    tok = ToktokTokenizer()
    return tok.tokenize(text)

feature_extraction_marathi = TfidfVectorizer(min_df=1, stop_words=stop_words_marathi, lowercase=True,ngram_range=(1,2),tokenizer=tokenizer)
feature_extraction = TfidfVectorizer(min_df=1,stop_words=stop_words,lowercase=True,ngram_range=(1,1),tokenizer=tokenizer_2)
feature_extraction_hindi = TfidfVectorizer(min_df=1, stop_words=hindi_stop_words, lowercase=True,ngram_range=(1,2),tokenizer=tokenizer_2)

feature_extraction.fit_transform(x)
feature_extraction_marathi.fit_transform(x_mar)
feature_extraction_hindi.fit_transform(x_hin)


def marathi(b):
    normalized_text = re.sub(r'\n+', '\n', b)
    normalized_text = re.sub(r'\s+', ' ', normalized_text).strip()
    normalized_text = re.sub(r'\(\S+\)', '', normalized_text)  # Remove text in parentheses
    normalized_text = re.sub(r'https?://\S+', '', normalized_text)  # Remove URLs
    input_message = [normalized_text]
    input_data_feature = feature_extraction_marathi.transform(input_message)
    input_data_feature = input_data_feature.toarray()
    byte_data = input_data_feature.tobytes()
    return byte_data

def english(b):
    normalized_text = re.sub(r'\n+', '\n', b)
    normalized_text = re.sub(r'\s+', ' ', normalized_text).strip()
    normalized_text = re.sub(r'\(\S+\)', '', normalized_text)  # Remove text in parentheses
    normalized_text = re.sub(r'https?://\S+', '', normalized_text)  # Remove URLs
    input_message = [normalized_text]
    input_data_feature = feature_extraction.transform(input_message)
    input_data_feature = input_data_feature.toarray()
    byte_data = input_data_feature.tobytes()
    return byte_data

def hindi(b):
    normalized_text = re.sub(r'\n+', '\n', b)
    normalized_text = re.sub(r'\s+', ' ', normalized_text).strip()
    normalized_text = re.sub(r'\(\S+\)', '', normalized_text)  # Remove text in parentheses
    normalized_text = re.sub(r'https?://\S+', '', normalized_text)  # Remove URLs
    input_message = [normalized_text]
    input_data_feature = feature_extraction_hindi.transform(input_message)
    input_data_feature = input_data_feature.toarray()
    byte_data = input_data_feature.tobytes()
    return byte_data


# def Result(float_array):
#     float_object = np.array(float_array)
#     if float_object.shape[0] != 1:
#         float_object = float_object.reshape((1, -1))  # Reshape to have batch size of 1
#     # Ensure the data type is float32
#     float_object = float_object.astype(np.float32)
#     a = tf.argmax(float_object,axis =1)
#     values = a.numpy()
#     for value in values:
#         result = value
#     return result
