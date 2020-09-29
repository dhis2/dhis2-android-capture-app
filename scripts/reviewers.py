import random
import requests
import json
import sys
import os

print("Checking if PR already has reviewers assigned")

BITRISE_PULL_REQUEST = os.environ.get('BITRISE_PULL_REQUEST')
GITHUB_RELEASE_API_TOKEN = os.environ.get('GITHUB_RELEASE_API_TOKEN')

# Checks is PR already has reviewers assigned
requested_reviewers = requests.get("https://api.github.com/repos/dhis2/dhis2-android-capture-app/pulls/%s/requested_reviewers?access_token=%s" %(BITRISE_PULL_REQUEST, GITHUB_RELEASE_API_TOKEN)).json()

if requested_reviewers['users']:
    print("Reviewers already assigned. exiting.")
    sys.exit()
else:
    has_reviews = requests.get("https://api.github.com/repos/dhis2/dhis2-android-capture-app/pulls/%s/reviews?access_token=%s" %(BITRISE_PULL_REQUEST, GITHUB_RELEASE_API_TOKEN)).json()
    if has_reviews:
        print("Reviewers already assigned. exiting.")
        sys.exit()

    print("Ready to assigned reviewers")
    pr_owner = requests.get("https://api.github.com/repos/dhis2/dhis2-android-capture-app/pulls/%s?access_token=%s" %(BITRISE_PULL_REQUEST, GITHUB_RELEASE_API_TOKEN)).json()['user']['login']
    quadram_devs = ["ferdyrod", "Balcan", "mmmateos"]
    dhis_devs = ["JaimeToca", "andresmr"]

    if (pr_owner in quadram_devs):
        quadram_devs.remove(pr_owner)
    else:
        dhis_devs.remove(pr_owner)
    quadram_reviewer = random.choice(quadram_devs)
    dhis_reviewer = random.choice(dhis_devs)

    reviewers = json.dumps([quadram_reviewer, dhis_reviewer])

    print("Assigning reviewers selected to the PR")
    payload = '{"reviewers":%s}' %(reviewers)
    requests.post("https://api.github.com/repos/dhis2/dhis2-android-capture-app/pulls/%s/requested_reviewers?access_token=%s" %(BITRISE_PULL_REQUEST, GITHUB_RELEASE_API_TOKEN), data=payload)
