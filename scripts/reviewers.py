import random
import requests
import json
import sys
import os

print("Checking if PR already has reviewers assigned")

BITRISE_PULL_REQUEST = os.environ.get('BITRISE_PULL_REQUEST')
GITHUB_RELEASE_API_TOKEN = os.environ.get('GITHUB_RELEASE_API_TOKEN')
Headers = {"Authorization": "Token %s" %(GITHUB_RELEASE_API_TOKEN) }

# Checks is PR already has reviewers assigned
requested_reviewers = requests.get("https://api.github.com/repos/dhis2/dhis2-android-capture-app/pulls/%s/requested_reviewers" %(BITRISE_PULL_REQUEST), headers=Headers).json()

if requested_reviewers['users']:
    print("Reviewers already assigned. exiting.")
    sys.exit()
else:
    has_reviews = requests.get("https://api.github.com/repos/dhis2/dhis2-android-capture-app/pulls/%s/reviews" %(BITRISE_PULL_REQUEST), headers=Headers).json()
    if has_reviews:
        print("Reviewers already assigned. exiting.")
        sys.exit()

    print("Ready to assigned reviewers")
    pr_owner = requests.get("https://api.github.com/repos/dhis2/dhis2-android-capture-app/pulls/%s" %(BITRISE_PULL_REQUEST), headers=Headers).json()['user']['login']
    devs = ["ferdyrod", "Balcan", "mmmateos", "andresmr"]

    if pr_owner in devs:
        devs.remove(pr_owner)

    random_devs = random.sample(devs, 2)
    reviewers = json.dumps(random_devs)

    print("Assigning reviewers selected to the PR")
    payload = '{"reviewers":%s}' %(reviewers)
    response = requests.post("https://api.github.com/repos/dhis2/dhis2-android-capture-app/pulls/%s/requested_reviewers" %(BITRISE_PULL_REQUEST), data=payload, headers=Headers)