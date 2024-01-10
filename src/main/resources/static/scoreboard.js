const ENDPOINTS = {
    LOAD: '/api/scoreboard/players',
}

function scoreBoard() {
    fetch("/api/scoreboard/players")
        .then(response => response.json())
        .then(data => {
            data.forEach((element) => console.log(element));

            data.forEach((player) => {
            const row = document.createElement('tr');

            const playerNameCell = document.createElement('td');
            playerNameCell.textContent = player.playerName;
            playerNameCell.classList.add('nameCell');
            row.appendChild(playerNameCell);

            const playerScoreCell = document.createElement('td');
            playerScoreCell.textContent = player.playerScore;
            playerScoreCell.classList.add('scoreCell');
            row.appendChild(playerScoreCell);

            scoreboard.appendChild(row);
            });

        });
}



window.onload = () => {
    scoreBoard()
}