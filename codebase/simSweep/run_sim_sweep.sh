python3 -m venv .venv && source .venv/bin/activate
pip install -r requirements.txt
python sim_sweep.py --base-url http://localhost:8080/api/simulation --sample-size 10000