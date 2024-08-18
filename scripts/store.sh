#!/bin/bash

# Function to list the available files on the server
list_files() {
    echo "Listing files on the server..."
    curl http://localhost:8080/files
}

# Function to download a file from the server
download_file() {
    if [ -z "$1" ]; then
        echo "Please provide the filename to download."
        return
    fi

    echo "Downloading file: $1"
    curl --fail -O http://localhost:8080/files/download/"$1"
    if [ $? -ne 0 ]; then
        echo "Failed to download the file."
    fi
}

# Function to upload multiple files to the server
upload_files() {
    if [ $# -lt 1 ]; then
        echo "Please provide at least one file to upload."
        return
    fi

    echo "Uploading files: $@"
    upload_args=()
    for file in "$@"; do
        upload_args+=("-F" "files=@$file")
    done

    curl "${upload_args[@]}" http://localhost:8080/files/upload
}

# Function to delete a file from the server
delete_file() {
    if [ -z "$1" ]; then
        echo "Please provide the filename to delete."
        return
    fi

    echo "Deleting file: $1"
    curl -X DELETE http://localhost:8080/files/delete/"$1"
}

# Function to update a file on the server
update_file() {
    if [ -z "$1" ]; then
        echo "Please provide the file to update."
        return
    fi

    echo "Updating file: $1"
    curl -F "file=@$1" http://localhost:8080/files/update
}

# Function to get the total word count of all files on the server
word_count() {
    echo "Getting total word count of all files on the server..."
    curl http://localhost:8080/files/wordcount
}

# Function to get the most frequent words from all files on the server
frequent_words() {
    echo "Getting the 10 most frequent words from all files on the server..."
    curl http://localhost:8080/files/frequentwords
}

# Main logic to handle commands
case "$1" in
    ls)
        list_files
        ;;
    add)
        shift
        upload_files "$@"
        ;;
    rm)
        shift
        delete_file "$1"
        ;;
    dl)
        shift
        download_file "$1"
        ;;
    update)
        shift
        update_file "$1"
        ;;
    wc)
        word_count
        ;;
    freq-words)
        frequent_words
        ;;
    *)
        echo "Usage:"
        echo "  store ls                    - List all files"
        echo "  store add file1 file2       - Upload files"
        echo "  store rm filename           - Remove a file"
        echo "  store dl filename           - Download a file"
        echo "  store update filename       - Update a file"
        echo "  store wc                    - Get total word count"
        echo "  store freq-words            - Get most frequent words"
        ;;
esac

